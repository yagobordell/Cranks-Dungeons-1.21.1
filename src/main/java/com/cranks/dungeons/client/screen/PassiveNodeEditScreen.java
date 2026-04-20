package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.config.PassiveTreeConfig;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.stat.passive.PassiveNode;
import com.cranks.dungeons.stat.passive.PassiveNodeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.input.KeyInput;

import java.util.*;
import java.util.stream.Collectors;

public class PassiveNodeEditScreen extends Screen {
    private final PassiveSkillTreeScreen parent;
    private final PassiveNode nodeToEdit; // null if creating
    private final double gridX;
    private final double gridY;

    private TextFieldWidget idField;
    private TextFieldWidget nameField;
    private TextFieldWidget descField;
    private TextFieldWidget shapeField;
    private TextFieldWidget statsField;

    private List<String> allAttributeIds = new ArrayList<>();
    private List<String> filteredSuggestions = new ArrayList<>();
    private int selectedSuggestion = 0;
    private boolean showSuggestions = false;

    public PassiveNodeEditScreen(PassiveSkillTreeScreen parent, PassiveNode node) {
        super(Text.literal("Edit Node: " + (node != null ? node.id() : "New Node")));
        this.parent = parent;
        this.nodeToEdit = node;
        this.gridX = node != null ? node.gridX() : 0;
        this.gridY = node != null ? node.gridY() : 0;
    }

    public PassiveNodeEditScreen(PassiveSkillTreeScreen parent, double gx, double gy) {
        super(Text.literal("Create New Node"));
        this.parent = parent;
        this.nodeToEdit = null;
        this.gridX = gx;
        this.gridY = gy;
    }

    @Override
    protected void init() {
        allAttributeIds = Registries.ATTRIBUTE.getIds().stream().map(Identifier::toString).sorted().toList();
        
        int centerX = this.width / 2;
        int startY = 40;
        int fieldW = 200;

        // ID Field
        idField = new TextFieldWidget(this.textRenderer, centerX - fieldW/2, startY, fieldW, 20, Text.literal("ID"));
        idField.setMaxLength(64);
        idField.setText(nodeToEdit != null ? nodeToEdit.id() : "new_node_id");
        idField.setEditable(nodeToEdit == null); // Can't rename ID of existing node safely here
        this.addDrawableChild(idField);
        startY += 30;

        // Name Field
        nameField = new TextFieldWidget(this.textRenderer, centerX - fieldW/2, startY, fieldW, 20, Text.literal("Name"));
        nameField.setText(nodeToEdit != null ? nodeToEdit.name() : "New Node Name");
        this.addDrawableChild(nameField);
        startY += 30;

        // Desc Field
        descField = new TextFieldWidget(this.textRenderer, centerX - fieldW/2, startY, fieldW, 20, Text.literal("Description"));
        descField.setMaxLength(256);
        descField.setText(nodeToEdit != null ? nodeToEdit.description() : "Node description...");
        this.addDrawableChild(descField);
        startY += 30;

        // Shape Field
        shapeField = new TextFieldWidget(this.textRenderer, centerX - fieldW/2, startY, fieldW, 20, Text.literal("Shape"));
        shapeField.setText(nodeToEdit != null ? nodeToEdit.shape() : "square");
        this.addDrawableChild(shapeField);
        startY += 30;

        // Modifiers Field (Format: attr:id=val,attr:id2=val2)
        statsField = new TextFieldWidget(this.textRenderer, centerX - fieldW/2, startY, fieldW, 20, Text.literal("Modifiers"));
        statsField.setMaxLength(512);
        if (nodeToEdit != null) {
            String modStr = nodeToEdit.modifiers().entrySet().stream()
                    .map(e -> Registries.ATTRIBUTE.getId(e.getKey().value()).toString() + "=" + e.getValue())
                    .collect(Collectors.joining(","));
            statsField.setText(modStr);
        }
        this.addDrawableChild(statsField);
        startY += 40;

        // Save Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save Settings"), b -> {
            saveNode();
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(centerX - 105, startY, 100, 20).build());

        // Cancel Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> {
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(centerX + 5, startY, 100, 20).build());

        startY += 25;

        // EXTRA BUTTONS FOR EXISTING NODES
        if (nodeToEdit != null) {
            // Delete Button
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Delete Node").formatted(Formatting.RED), b -> {
                PassiveNodeRegistry.getNodes().remove(nodeToEdit.id());
                // Scrub connections
                for (PassiveNode other : PassiveNodeRegistry.getNodes().values()) {
                    other.connections().remove(nodeToEdit.id());
                }
                parent.refreshCachesProxy();
                MinecraftClient.getInstance().setScreen(parent);
            }).dimensions(centerX - 105, startY, 100, 20).build());

            // Connect Button
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Connect/Link"), b -> {
                saveNode(); // Save current state first
                parent.startConnecting(PassiveNodeRegistry.getNodes().get(idField.getText()));
            }).dimensions(centerX + 5, startY, 100, 20).build());
        }
    }

    private void saveNode() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String desc = descField.getText().trim();
        String shape = shapeField.getText().trim();
        Map<RegistryEntry<EntityAttribute>, Double> mods = parseModifiers(statsField.getText());

        if (nodeToEdit != null) {
            // Update existing
            nodeToEdit.setGridX(gridX);
            nodeToEdit.setGridY(gridY);
            // We'd need more setters if we wanted to change desc/name on the fly, 
            // but for simplicity in this dev tool we can just replace the node in the registry
            PassiveNode newNode = new PassiveNode(id, name, desc, gridX, gridY, 
                    nodeToEdit.connections(), mods, nodeToEdit.icon(), shape);
            PassiveNodeRegistry.getNodes().put(id, newNode);
        } else {
            // Create new
            PassiveNode newNode = new PassiveNode(id, name, desc, gridX, gridY, 
                    new ArrayList<>(), mods, null, shape);
            PassiveNodeRegistry.getNodes().put(id, newNode);
        }
        parent.refreshCachesProxy(); // Trigger UI refresh
    }

    private Map<RegistryEntry<EntityAttribute>, Double> parseModifiers(String text) {
        Map<RegistryEntry<EntityAttribute>, Double> map = new LinkedHashMap<>();
        if (text.isEmpty()) return map;
        
        String[] parts = text.split(",");
        for (String part : parts) {
            String[] kv = part.split("=");
            if (kv.length == 2) {
                Identifier attrId = Identifier.tryParse(kv[0].trim());
                if (attrId != null) {
                    Registries.ATTRIBUTE.getOptionalValue(attrId).ifPresent(attr -> {
                        map.put(Registries.ATTRIBUTE.getEntry(attr), Double.parseDouble(kv[1].trim()));
                    });
                }
            }
        }
        return map;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Use fill instead of renderBackground to avoid "Can only blur once per frame" crash
        context.fill(0, 0, this.width, this.height, 0x90000000);
        context.fill(10, 10, this.width - 10, this.height - 10, 0x80000000);
        
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        
        context.drawTextWithShadow(this.textRenderer, "Node ID:", this.width / 2 - 100, idField.getY() - 12, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "Display Name:", this.width / 2 - 100, nameField.getY() - 12, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "Description:", this.width / 2 - 100, descField.getY() - 12, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "Shape (square/circle/diamond/hexagon):", this.width / 2 - 100, shapeField.getY() - 12, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "Modifiers (attr=val,attr2=val2):", this.width / 2 - 100, statsField.getY() - 12, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);

        renderSuggestions(context);
    }

    private void renderSuggestions(DrawContext context) {
        updateSuggestions();
        if (!showSuggestions || filteredSuggestions.isEmpty()) return;

        int x = statsField.getX();
        int y = statsField.getY() + statsField.getHeight();
        int w = statsField.getWidth();
        int h = Math.min(filteredSuggestions.size(), 5) * 12 + 2;

        context.fill(x, y, x + w, y + h, 0xFF000000);
        // Draw white border using 4 fill calls
        context.fill(x, y, x + w, y + 1, 0xFFFFFFFF); // TOP
        context.fill(x, y + h - 1, x + w, y + h, 0xFFFFFFFF); // BOTTOM
        context.fill(x, y, x + 1, y + h, 0xFFFFFFFF); // LEFT
        context.fill(x + w - 1, y, x + w, y + h, 0xFFFFFFFF); // RIGHT

        for (int i = 0; i < Math.min(filteredSuggestions.size(), 5); i++) {
            int color = (i == selectedSuggestion) ? 0xFFFFFF00 : 0xFFFFFFFF;
            context.drawTextWithShadow(textRenderer, filteredSuggestions.get(i), x + 5, y + 2 + i * 12, color);
        }
    }

    private void updateSuggestions() {
        if (!statsField.isFocused()) {
            showSuggestions = false;
            return;
        }

        String text = statsField.getText();
        int cursor = statsField.getCursor();
        
        // Find current word being typed
        int start = Math.max(0, text.lastIndexOf(',', cursor - 1) + 1);
        String currentPart = text.substring(start, cursor).trim();
        
        if (currentPart.contains("=")) {
            showSuggestions = false;
            return;
        }

        if (currentPart.isEmpty()) {
            showSuggestions = false;
            return;
        }

        filteredSuggestions = allAttributeIds.stream()
                .filter(id -> id.contains(currentPart))
                .limit(10)
                .collect(Collectors.toList());
        
        showSuggestions = !filteredSuggestions.isEmpty();
        if (selectedSuggestion >= filteredSuggestions.size()) selectedSuggestion = 0;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();
        if (showSuggestions && !filteredSuggestions.isEmpty()) {
            if (keyCode == 264) { // DOWN
                selectedSuggestion = (selectedSuggestion + 1) % Math.min(filteredSuggestions.size(), 5);
                return true;
            }
            if (keyCode == 265) { // UP
                selectedSuggestion = (selectedSuggestion - 1 + Math.min(filteredSuggestions.size(), 5)) % Math.min(filteredSuggestions.size(), 5);
                return true;
            }
            if (keyCode == 257 || keyCode == 258) { // ENTER or TAB
                applySuggestion();
                return true;
            }
        }
        return super.keyPressed(input);
    }

    private void applySuggestion() {
        String text = statsField.getText();
        int cursor = statsField.getCursor();
        int start = Math.max(0, text.lastIndexOf(',', cursor - 1) + 1);
        
        String suggestion = filteredSuggestions.get(selectedSuggestion);
        String newText = text.substring(0, start) + suggestion + text.substring(cursor);
        statsField.setText(newText);
        statsField.setSelectionStart(start + suggestion.length());
        statsField.setSelectionEnd(start + suggestion.length());
        showSuggestions = false;
    }
}
