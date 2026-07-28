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

    private final PassiveNode nodeToEdit; // null if creating
    private final double gridX;
    private final double gridY;

    private TextFieldWidget idField;
    private TextFieldWidget nameField;
    private TextFieldWidget descField;
    private TextFieldWidget shapeField;

    private List<String> allAttributeIds = new ArrayList<>();
    private List<String> filteredSuggestions = new ArrayList<>();
    private int selectedSuggestion = 0;
    private boolean showSuggestions = false;
    
    private boolean isAttrSelection = false;
    private ButtonWidget toggleAttrBtn;
    private TextFieldWidget attrAmountField;

    private List<ModifierRow> modifierRows = new ArrayList<>();
    private ButtonWidget addModBtn;

    private ButtonWidget saveBtn;
    private ButtonWidget cancelBtn;
    private ButtonWidget deleteBtn;
    private ButtonWidget connectBtn;

    private class ModifierRow {
        TextFieldWidget attrField;
        TextFieldWidget valField;
        ButtonWidget removeBtn;

        ModifierRow(String attr, String val, int index) {
            int rightX = width / 2 + 10;
            int y = 40 + (index * 25);
            
            attrField = new TextFieldWidget(textRenderer, rightX, y, 135, 20, Text.literal("Attribute"));
            attrField.setMaxLength(128);
            attrField.setText(attr);

            valField = new TextFieldWidget(textRenderer, rightX + 140, y, 40, 20, Text.literal("Value"));
            valField.setMaxLength(32);
            valField.setText(val);

            removeBtn = ButtonWidget.builder(Text.literal("X").formatted(Formatting.RED), b -> {
                modifierRows.remove(this);
                rebuildRows();
            }).dimensions(rightX + 185, y, 20, 20).build();
        }
    }

    private final net.minecraft.client.gui.screen.Screen parentScreen;
    private final Runnable refreshCallback;
    private final java.util.function.Consumer<PassiveNode> startConnectingCallback;

    public PassiveNodeEditScreen(net.minecraft.client.gui.screen.Screen parentScreen, PassiveNode node, Runnable refreshCallback, java.util.function.Consumer<PassiveNode> startConnectingCallback) {
        super(Text.literal("Edit Node: " + (node != null ? node.id() : "New Node")));
        this.parentScreen = parentScreen;
        this.refreshCallback = refreshCallback;
        this.startConnectingCallback = startConnectingCallback;
        this.nodeToEdit = node;
        this.gridX = node != null ? node.gridX() : 0;
        this.gridY = node != null ? node.gridY() : 0;
    }

    public PassiveNodeEditScreen(net.minecraft.client.gui.screen.Screen parentScreen, double gx, double gy, Runnable refreshCallback, java.util.function.Consumer<PassiveNode> startConnectingCallback) {
        super(Text.literal("Create New Node"));
        this.parentScreen = parentScreen;
        this.refreshCallback = refreshCallback;
        this.startConnectingCallback = startConnectingCallback;
        this.nodeToEdit = null;
        this.gridX = gx;
        this.gridY = gy;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parentScreen);
    }

    @Override
    protected void init() {
        allAttributeIds = Registries.ATTRIBUTE.getIds().stream()
                .filter(id -> id.getNamespace().equals("cranks-dungeons"))
                .map(Identifier::getPath)
                .filter(id -> com.cranks.dungeons.stat.passive.PassiveStatHelper.WHITELISTED_PASSIVE_ATTRIBUTES.contains(id))
                .sorted()
                .toList();
        
        int centerX = this.width / 2;
        int leftX = centerX - 210;
        int rightX = centerX + 10;
        int fieldW = 200;

        idField = new TextFieldWidget(this.textRenderer, leftX, 40, fieldW, 20, Text.literal("ID"));
        idField.setMaxLength(64);
        if (nodeToEdit != null) {
            idField.setText(nodeToEdit.id());
        } else {
            int maxId = 0;
            for (String key : PassiveNodeRegistry.getNodes().keySet()) {
                if (key.startsWith("node_")) {
                    try {
                        int num = Integer.parseInt(key.substring(5));
                        if (num > maxId) maxId = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
            idField.setText("node_" + (maxId + 1));
        }
        idField.setEditable(false);
        this.addDrawableChild(idField);

        nameField = new TextFieldWidget(this.textRenderer, leftX, 85, fieldW, 20, Text.literal("Name"));
        if (nodeToEdit != null) nameField.setText(nodeToEdit.name());
        this.addDrawableChild(nameField);

        descField = new TextFieldWidget(this.textRenderer, leftX, 130, fieldW, 20, Text.literal("Description"));
        descField.setMaxLength(256);
        if (nodeToEdit != null) descField.setText(nodeToEdit.description());
        this.addDrawableChild(descField);

        shapeField = new TextFieldWidget(this.textRenderer, leftX, 175, fieldW, 20, Text.literal("Shape"));
        if (nodeToEdit != null) shapeField.setText(nodeToEdit.shape());
        this.addDrawableChild(shapeField);

        isAttrSelection = nodeToEdit != null && nodeToEdit.isAttributeSelection();
        toggleAttrBtn = ButtonWidget.builder(Text.literal("Is Attr Choice: " + isAttrSelection), b -> {
            isAttrSelection = !isAttrSelection;
            b.setMessage(Text.literal("Is Attr Choice: " + isAttrSelection));
            rebuildRows();
        }).dimensions(leftX, 220, fieldW, 20).build();
        this.addDrawableChild(toggleAttrBtn);

        attrAmountField = new TextFieldWidget(this.textRenderer, rightX, 40, fieldW, 20, Text.literal("Amount"));
        attrAmountField.setText(nodeToEdit != null ? String.valueOf(nodeToEdit.attributeSelectionAmount()) : "");
        this.addDrawableChild(attrAmountField);
        
        // Modifiers loading
        modifierRows.clear();
        if (nodeToEdit != null) {
            int i = 0;
            for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : nodeToEdit.modifiers().entrySet()) {
                if (i >= 5) break;
                String attr = Registries.ATTRIBUTE.getId(entry.getKey().value()).toString();
                if (attr.startsWith("minecraft:")) continue;
                if (attr.startsWith("cranks-dungeons:")) {
                    attr = attr.substring(16);
                }
                String val = String.valueOf(entry.getValue());
                modifierRows.add(new ModifierRow(attr, val, i));
                i++;
            }
        }
        
        if (modifierRows.isEmpty()) {
            modifierRows.add(new ModifierRow("", "", 0));
        }

        addModBtn = ButtonWidget.builder(Text.literal("+ Add Modifier"), b -> {
            if (modifierRows.size() < 5) {
                modifierRows.add(new ModifierRow("", "", modifierRows.size()));
                rebuildRows();
            }
        }).dimensions(rightX, 165, 205, 20).build();

        int btnY = 270;
        if (nodeToEdit != null) {
            saveBtn = ButtonWidget.builder(Text.literal("Save Settings"), b -> {
                saveNode();
                MinecraftClient.getInstance().setScreen(parentScreen);
            }).dimensions(centerX - 210, btnY, 100, 20).build();
            
            cancelBtn = ButtonWidget.builder(Text.literal("Cancel"), b -> {
                MinecraftClient.getInstance().setScreen(parentScreen);
            }).dimensions(centerX - 100, btnY, 100, 20).build();

            deleteBtn = ButtonWidget.builder(Text.literal("Delete Node").formatted(Formatting.RED), b -> {
                PassiveNodeRegistry.getNodes().remove(nodeToEdit.id());
                for (PassiveNode other : PassiveNodeRegistry.getNodes().values()) {
                    other.connections().remove(nodeToEdit.id());
                }
                if (refreshCallback != null) refreshCallback.run();
                MinecraftClient.getInstance().setScreen(parentScreen);
            }).dimensions(centerX + 10, btnY, 100, 20).build();

            connectBtn = ButtonWidget.builder(Text.literal("Connect/Link"), b -> {
                saveNode();
                if (startConnectingCallback != null) {
                    startConnectingCallback.accept(PassiveNodeRegistry.getNodes().get(idField.getText()));
                }
            }).dimensions(centerX + 120, btnY, 100, 20).build();
            
            this.addDrawableChild(deleteBtn);
            this.addDrawableChild(connectBtn);
        } else {
            saveBtn = ButtonWidget.builder(Text.literal("Create Node"), b -> {
                saveNode();
                MinecraftClient.getInstance().setScreen(parentScreen);
            }).dimensions(centerX - 105, btnY, 100, 20).build();
            
            cancelBtn = ButtonWidget.builder(Text.literal("Cancel"), b -> {
                MinecraftClient.getInstance().setScreen(parentScreen);
            }).dimensions(centerX + 5, btnY, 100, 20).build();
        }

        this.addDrawableChild(saveBtn);
        this.addDrawableChild(cancelBtn);

        rebuildRows();
    }

    private void rebuildRows() {
        for (Object child : new ArrayList<>(this.children())) {
            if (child instanceof TextFieldWidget || child instanceof ButtonWidget) {
                if (child != idField && child != nameField && child != descField && child != shapeField
                        && child != toggleAttrBtn
                        && child != saveBtn && child != cancelBtn
                        && child != deleteBtn && child != connectBtn) {
                    this.remove((net.minecraft.client.gui.Element) child);
                }
            }
        }
        
        if (isAttrSelection) {
            this.addDrawableChild(attrAmountField);
            attrAmountField.setVisible(true);
        } else {
            attrAmountField.setVisible(false);
            int i = 0;
            for (ModifierRow row : modifierRows) {
                int y = 40 + (i * 25);
                row.attrField.setY(y);
                row.valField.setY(y);
                row.removeBtn.setY(y);
                
                row.attrField.setVisible(true);
                row.valField.setVisible(true);
                row.removeBtn.active = true;
                
                this.addDrawableChild(row.attrField);
                this.addDrawableChild(row.valField);
                this.addDrawableChild(row.removeBtn);
                i++;
            }
            
            int bottomY = 40 + (modifierRows.size() * 25);
            addModBtn.setY(bottomY);
            if (modifierRows.size() < 5) {
                this.addDrawableChild(addModBtn);
            }
        }
    }

    private void saveNode() {
        String id = idField.getText().trim();
        if (id.isEmpty()) id = "new_node";
        String name = nameField.getText().trim();
        String desc = descField.getText().trim();
        String shape = shapeField.getText().trim();
        if (shape.isEmpty()) shape = "square";

        Map<RegistryEntry<EntityAttribute>, Double> mods = new LinkedHashMap<>();
        if (!isAttrSelection) {
            for (ModifierRow row : modifierRows) {
                String attr = row.attrField.getText().trim();
                String valStr = row.valField.getText().trim();
                if (!attr.isEmpty() && !valStr.isEmpty()) {
                    if (!attr.contains(":")) {
                        attr = "cranks-dungeons:" + attr;
                    }
                    if (attr.startsWith("minecraft:")) continue;
                    try {
                        double val = Double.parseDouble(valStr);
                        Identifier attrId = Identifier.tryParse(attr);
                        if (attrId != null) {
                            Registries.ATTRIBUTE.getOptionalValue(attrId).ifPresent(a -> {
                                mods.put(Registries.ATTRIBUTE.getEntry(a), val);
                            });
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        
        double attrAmount = 0.0;
        if (isAttrSelection) {
            try {
                attrAmount = Double.parseDouble(attrAmountField.getText().trim());
            } catch (NumberFormatException ignored) {}
        }

        Identifier calculatedIcon = com.cranks.dungeons.stat.passive.PassiveIconManager.getIcon(id, mods, isAttrSelection);

        if (nodeToEdit != null) {
            nodeToEdit.setGridX(gridX);
            nodeToEdit.setGridY(gridY);
            PassiveNode newNode = new PassiveNode(id, name, desc, gridX, gridY, 
                    nodeToEdit.connections(), mods, calculatedIcon, shape,
                    isAttrSelection, attrAmount);
            PassiveNodeRegistry.getNodes().put(id, newNode);
        } else {
            PassiveNode newNode = new PassiveNode(id, name, desc, gridX, gridY, 
                    new ArrayList<>(), mods, calculatedIcon, shape, isAttrSelection, attrAmount);
            PassiveNodeRegistry.getNodes().put(id, newNode);
        }
        if (refreshCallback != null) refreshCallback.run();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x90000000);
        context.fill(10, 10, this.width - 10, this.height - 10, 0x80000000);
        
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        
        int leftX = this.width / 2 - 210;
        int rightX = this.width / 2 + 10;

        context.drawTextWithShadow(this.textRenderer, "Node ID (Auto-generated):", leftX, idField.getY() - 12, 0xFFAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "Display Name:", leftX, nameField.getY() - 12, 0xFFAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "Description:", leftX, descField.getY() - 12, 0xFFAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "Shape (square/circle/diamond/hexagon):", leftX, shapeField.getY() - 12, 0xFFAAAAAA);
        
        if (isAttrSelection) {
            context.drawTextWithShadow(this.textRenderer, "Attribute Amount:", rightX, attrAmountField.getY() - 12, 0xFFAAAAAA);
        } else {
            context.drawTextWithShadow(this.textRenderer, "Modifiers (Attr ID & Value):", rightX, 40 - 12, 0xFFAAAAAA);
        }

        super.render(context, mouseX, mouseY, delta);
        
        // Render Placeholders
        drawPlaceholder(context, nameField, "e.g. Brawler's Might");
        drawPlaceholder(context, descField, "e.g. Grants +5 Strength");
        drawPlaceholder(context, shapeField, "e.g. square");
        
        if (isAttrSelection) {
            drawPlaceholder(context, attrAmountField, "e.g. 5.0");
        } else {
            for (ModifierRow row : modifierRows) {
                drawPlaceholder(context, row.attrField, "e.g. max_health");
                drawPlaceholder(context, row.valField, "5.0");
            }
        }

        renderSuggestions(context);
    }
    
    private void drawPlaceholder(DrawContext context, TextFieldWidget field, String text) {
        if (field.isVisible() && field.getText().isEmpty() && !field.isFocused()) {
            context.drawTextWithShadow(this.textRenderer, text, field.getX() + 4, field.getY() + 6, 0xFF888888);
        }
    }

    private TextFieldWidget getActiveAttrField() {
        if (isAttrSelection) return null;
        for (ModifierRow row : modifierRows) {
            if (row.attrField.isFocused()) {
                return row.attrField;
            }
        }
        return null;
    }

    private void renderSuggestions(DrawContext context) {
        updateSuggestions();
        if (!showSuggestions || filteredSuggestions.isEmpty()) return;
        
        TextFieldWidget activeField = getActiveAttrField();
        if (activeField == null) return;

        int x = activeField.getX();
        int y = activeField.getY() + activeField.getHeight();
        int w = activeField.getWidth();
        int h = Math.min(filteredSuggestions.size(), 5) * 12 + 2;

        if (y + h > this.height) {
            y = activeField.getY() - h;
        }

        context.fill(x, y, x + w, y + h, 0xFF000000);
        context.fill(x, y, x + w, y + 1, 0xFFFFFFFF);
        context.fill(x, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        context.fill(x, y, x + 1, y + h, 0xFFFFFFFF);
        context.fill(x + w - 1, y, x + w, y + h, 0xFFFFFFFF);

        for (int i = 0; i < Math.min(filteredSuggestions.size(), 5); i++) {
            int color = (i == selectedSuggestion) ? 0xFFFFFF00 : 0xFFFFFFFF;
            context.drawTextWithShadow(textRenderer, filteredSuggestions.get(i), x + 5, y + 2 + i * 12, color);
        }
    }

    private void updateSuggestions() {
        TextFieldWidget activeField = getActiveAttrField();
        if (activeField == null) {
            showSuggestions = false;
            return;
        }

        String text = activeField.getText().trim();

        if (text.isEmpty()) {
            filteredSuggestions = allAttributeIds.stream().limit(10).collect(Collectors.toList());
            showSuggestions = true;
            if (selectedSuggestion >= filteredSuggestions.size()) selectedSuggestion = 0;
            return;
        }

        filteredSuggestions = allAttributeIds.stream()
                .filter(id -> id.contains(text))
                .limit(10)
                .collect(Collectors.toList());
        
        showSuggestions = !filteredSuggestions.isEmpty();
        if (selectedSuggestion >= filteredSuggestions.size()) selectedSuggestion = 0;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();
        if (showSuggestions && !filteredSuggestions.isEmpty()) {
            if (keyCode == 264) {
                selectedSuggestion = (selectedSuggestion + 1) % Math.min(filteredSuggestions.size(), 5);
                return true;
            }
            if (keyCode == 265) {
                selectedSuggestion = (selectedSuggestion - 1 + Math.min(filteredSuggestions.size(), 5)) % Math.min(filteredSuggestions.size(), 5);
                return true;
            }
            if (keyCode == 257 || keyCode == 258) {
                applySuggestion();
                return true;
            }
        }
        return super.keyPressed(input);
    }

    private void applySuggestion() {
        TextFieldWidget activeField = getActiveAttrField();
        if (activeField == null || filteredSuggestions.isEmpty()) return;
        
        String suggestion = filteredSuggestions.get(selectedSuggestion);
        activeField.setText(suggestion);
        activeField.setSelectionStart(suggestion.length());
        activeField.setSelectionEnd(suggestion.length());
        showSuggestions = false;
    }
}
