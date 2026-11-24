package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumButtonWidget;
import com.cranks.dungeons.client.compendium.CompendiumScreen;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.client.InventoryStyle.Panel;
import com.cranks.dungeons.inventory.GearSalvage;
import com.cranks.dungeons.equipment.EnchantmentSlotHelper;
import com.cranks.dungeons.inventory.EquipmentRepair;
import com.cranks.dungeons.inventory.PlayerLoadout;
import com.cranks.dungeons.mount.SlotPositionAccess;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import com.cranks.dungeons.network.EnchantGearPayload;
import com.cranks.dungeons.network.RepairGearPayload;
import com.cranks.dungeons.network.SalvageGearPayload;
import com.cranks.dungeons.screen.LoadoutAnvilHandler;
import com.cranks.dungeons.screen.LoadoutCraftingTableHandler;
import com.cranks.dungeons.screen.LoadoutCuttingBenchHandler;
import com.cranks.dungeons.screen.LoadoutEnchantingHandler;
import com.cranks.dungeons.screen.LoadoutFurnaceHandler;
import com.cranks.dungeons.screen.LoadoutGrindstoneHandler;
import com.cranks.dungeons.screen.LoadoutStationHandler;
import com.cranks.dungeons.screen.QualityStationScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public final class PlayerLoadoutScreen extends HandledScreen<ScreenHandler> {
    private static final int GRID = InventoryStyle.SLOT_PITCH;
    private static final int BAG_X = 69, BAG_Y = 45;
    private static final int BAG_SOCKET_Y = 19;
    private static final int GEAR_Y = 148;
    private static final int MAIN_X = 121;
    private static final int HOTBAR_X = 98, HOTBAR_Y = 217;
    private static final int[] SET_X = {11, 75};
    private static final int STATS_WIDTH = 154;
    private static final int ITEM_SIZE = 16;
    private static final int CRAFTING_GAP = 9;
    private static final int SOCKET_SIZE = 22;
    private static final int SOCKET_PITCH = SOCKET_SIZE + 5;
    // The bag sockets and wallet share a row, aligned to the storage frame's right edge.
    private static final int WALLET_X = BAG_X + PlayerLoadout.BAG_WIDTH * GRID + 2 - WalletDisplay.WIDTH;
    private static final int FIRST_BAG_SOCKET_X = WALLET_X - 4
            - ((PlayerLoadout.BAG_COUNT - 1) * SOCKET_PITCH + SOCKET_SIZE) + 3;
    private static final int FIRST_BAG_SOCKET = PlayerLoadout.handlerSlot(PlayerLoadout.EQUIPPED_BAGS);


    private final PlayerEntity player;
    private final PlayerInventory inventory;
    private final PlayerLoadout loadout;
    private final CharacterStatsPanel stats = new CharacterStatsPanel();
    private final InventoryStatusEffects effects = new InventoryStatusEffects();
    private final MaterialCraftingPanel crafting;
    private final CreativeCatalogPanel creative;
    private final TradePanel trade;
    private final WalletDisplay wallet=new WalletDisplay();
    private final RepairPanel repair;
    private final EnchantingPanel enchanting;
    private final AlchemyPanel alchemy;
    private final UpgradeStationPanel upgrade;
    private final RunicAltarPanel runic;
    private final ArtificialAltarPanel artificial;
    private final StoragePanel storage;
    private final CrafterPanel autoCrafter;
    private final MountPanel mountPanel;
    private final boolean furnace;
    // InvMove looks for a focused TextFieldWidget directly on the screen.
    private TextFieldWidget focusedTextField;
    private int stationTop;
    private boolean compact;
    private boolean compactCrafting;
    private CompendiumButtonWidget swap, compendiumButton, statsButton, viewToggle;
    private boolean visibleStats;
    private boolean initialLayout = true;
    private int blockedBag = -1;
    private long blockedUntil;
    private int salvageRequest;
    private GearConfirmation confirmation;
    private boolean confirmationClickCaptured;

    private record GearConfirmation(ItemStack item, ItemStack output, Text title, Text outcome, Text warning,
                                    Text action, Runnable confirm, java.util.List<Text> details) {
        GearConfirmation(ItemStack item, ItemStack output, Text title, Text outcome, Text warning, Text action, Runnable confirm) {
            this(item, output, title, outcome, warning, action, confirm, java.util.List.of());
        }
    }

    public PlayerLoadoutScreen(PlayerEntity player) {
        this(player.playerScreenHandler, player.getInventory(), Text.translatable("inventory.cranks-dungeons.title"));
    }

    public PlayerLoadoutScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.player = inventory.player;
        this.inventory = inventory;
        this.loadout = PlayerLoadout.of(inventory);
        this.trade=handler==player.playerScreenHandler&&TradeClient.visible()?new TradePanel():null;
        this.creative=trade==null&&handler==player.playerScreenHandler&&player.isCreative()?new CreativeCatalogPanel():null;
        boolean table = handler instanceof LoadoutCraftingTableHandler;
        boolean cutting = handler instanceof LoadoutCuttingBenchHandler;
        this.furnace = handler instanceof LoadoutFurnaceHandler;
        this.repair = handler instanceof LoadoutGrindstoneHandler grindstone
                ? new RepairPanel(grindstone, inventory, title, this::requestRepair) : null;
        this.enchanting = handler instanceof LoadoutEnchantingHandler tableHandler
                ? new EnchantingPanel(tableHandler, inventory, title, this::requestEnchant) : null;
        this.alchemy = handler instanceof com.cranks.dungeons.screen.LoadoutAlchemyHandler station ? new AlchemyPanel(station, inventory) : null;
        this.runic = handler instanceof com.cranks.dungeons.screen.RunicEnhancementAltarScreenHandler altar
                ? new RunicAltarPanel(altar, inventory, this::requestRunic) : null;
        this.artificial = handler instanceof com.cranks.dungeons.screen.ArtificialEnhancementAltarScreenHandler altar
                ? new ArtificialAltarPanel(altar, this::requestArtificial) : null;
        this.storage = handler instanceof com.cranks.dungeons.screen.LoadoutStorageHandler container ? new StoragePanel(container,title) : null;
        this.autoCrafter = handler instanceof com.cranks.dungeons.screen.LoadoutCrafterHandler machine ? new CrafterPanel(machine,inventory) : null;
        this.mountPanel = handler instanceof com.cranks.dungeons.screen.MountCargoScreenHandler mount ? new MountPanel(mount) : null;
        this.upgrade = handler instanceof QualityStationScreenHandler bench ? new UpgradeStationPanel(bench, title) : null;
        this.crafting = trade != null || creative != null || repair != null || upgrade != null || enchanting != null || alchemy != null || runic != null || artificial != null || storage != null || autoCrafter != null || mountPanel != null ? null : new MaterialCraftingPanel(inventory, table, cutting, handler.syncId, title,
                furnace ? (LoadoutFurnaceHandler) handler : null,
                handler instanceof LoadoutAnvilHandler, handler instanceof com.cranks.dungeons.screen.LoadoutFletchingHandler,
                handler instanceof com.cranks.dungeons.screen.LoadoutTailoringHandler,
                handler instanceof com.cranks.dungeons.screen.LoadoutCartographyHandler, this::requestInventorySalvage);
        this.compactCrafting = handler instanceof LoadoutStationHandler;
        backgroundWidth = 294;
        backgroundHeight = 240;
    }

    @Override
    protected void init() {
        backgroundHeight = Math.min(240, height - 4);
        super.init();
        player.currentScreenHandler = handler;
        int stationWidth = trade != null ? TradePanel.WIDTH : creative != null ? CreativeCatalogPanel.WIDTH : mountPanel != null ? MountPanel.WIDTH : autoCrafter != null ? CrafterPanel.WIDTH : storage != null ? StoragePanel.WIDTH : artificial != null ? ArtificialAltarPanel.WIDTH : runic != null ? RunicAltarPanel.WIDTH : MaterialCraftingPanel.WIDTH;
        int dockedWidth = backgroundWidth + CRAFTING_GAP + stationWidth;
        compact = width < dockedWidth + 12;
        int rightmost = width - dockedWidth - 6;
        if (!compact && x > rightmost) {
            x = rightmost >= STATS_WIDTH + 9 ? rightmost : (width - dockedWidth) / 2;
        }
        int panelWidth = Math.min(stationWidth, width - 12);
        int panelX = compact ? (width - panelWidth) / 2 : x + backgroundWidth + CRAFTING_GAP;
        int desiredStationHeight = mountPanel != null ? MountPanel.HEIGHT : autoCrafter != null ? CrafterPanel.HEIGHT
                : storage != null ? storage.desiredHeight() : artificial != null ? ArtificialAltarPanel.HEIGHT
                : runic != null ? RunicAltarPanel.HEIGHT : backgroundHeight - 10;
        int stationHeight = Math.min(desiredStationHeight, height - 16);
        stationTop = mountPanel != null || autoCrafter != null || storage != null || artificial != null || runic != null
                ? Math.max(8, (height - stationHeight) / 2) : y + 7;
        if(trade!=null)trade.init(textRenderer,panelX,y+7,panelWidth,backgroundHeight-10);
        else if (creative != null) creative.init(textRenderer,panelX,y+7,panelWidth,backgroundHeight-10,compact);
        else if (mountPanel != null) mountPanel.init(textRenderer,panelX,stationTop,panelWidth,Math.min(MountPanel.HEIGHT,height-16));
        else if (autoCrafter != null) autoCrafter.init(textRenderer,panelX,stationTop,panelWidth,Math.min(CrafterPanel.HEIGHT,height-16));
        else if (storage != null) storage.init(textRenderer,panelX,stationTop,panelWidth,compact);
        else if (artificial != null) artificial.init(textRenderer, panelX, stationTop, panelWidth, Math.min(ArtificialAltarPanel.HEIGHT, height - 16));
        else if (runic != null) runic.init(textRenderer, panelX, stationTop, panelWidth, Math.min(RunicAltarPanel.HEIGHT,height-16));
        else if (upgrade != null) upgrade.init(textRenderer, panelX, y + 7, panelWidth, backgroundHeight - 10);
        else if (repair != null) repair.init(textRenderer, panelX, y + 7, panelWidth, backgroundHeight - 10);
        else if (enchanting != null) enchanting.init(textRenderer, panelX, y + 7, panelWidth, backgroundHeight - 10);
        else if (alchemy != null) alchemy.init(textRenderer, panelX, y + 7, panelWidth, backgroundHeight - 10);
        else crafting.init(textRenderer, panelX, y + 7, panelWidth, backgroundHeight - 10, compact);
        if (initialLayout) {
            visibleStats = x >= STATS_WIDTH + 9;
            initialLayout = false;
        }
        stats.init(textRenderer, x >= STATS_WIDTH + 9 ? x - STATS_WIDTH - 7 : x + 5,
                y + 22, STATS_WIDTH, backgroundHeight - 24);
        placeSlots();
        compendiumButton = addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Compendium"), button -> openCompendium())
                .dimensions(x + 7, y + 2, 78, 15).clasp().build());
        statsButton = addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Stats"), button -> {
            visibleStats = !visibleStats;
            stats.endPointerInteraction();
            setFocused(null);
        }).dimensions(x + 7, y + 20, 51, 15).clasp().build());
        Text swapLabel = Text.translatable("inventory.cranks-dungeons.swap_armor", ModKeybinds.SWAP_ARMOR.getBoundKeyLocalizedText());
        swap = CompendiumButtonWidget.builder(swapLabel, button -> swapArmor())
                .dimensions(x + 52, y + GEAR_Y + 8, 17, 19).armorSwap().build();
        swap.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(swapLabel));
        addDrawableChild(swap);
        viewToggle = addDrawableChild(CompendiumButtonWidget.builder(Text.empty(), button -> {
            compactCrafting = !compactCrafting;
            stats.endPointerInteraction();
            setFocused(null);
            applyView(panelX, panelWidth);
        }).dimensions(x + 90, y + 2, 65, 15).clasp().build());
        applyView(panelX, panelWidth);
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        if (handler == player.playerScreenHandler && trade==null && (creative != null) != player.isCreative()) {
            client.setScreen(new PlayerLoadoutScreen(player));
        }
    }

    @Override
    public void removed() {
        if(creative!=null)creative.remember();
        super.removed();
    }

    @Override public void close(){if(trade!=null)TradeClient.close();super.close();}

    private boolean showsInventory() { return !compact || !compactCrafting; }

    private boolean showsCrafting() { return !compact || compactCrafting; }

    private boolean showsStats() { return showsInventory() && visibleStats; }

    private void applyView(int panelX, int panelWidth) {
        boolean inventoryVisible = showsInventory();
        compendiumButton.visible = inventoryVisible;
        statsButton.visible = inventoryVisible;
        swap.visible = inventoryVisible;
        viewToggle.visible = compact;
        boolean cutting = handler instanceof LoadoutCuttingBenchHandler;
        viewToggle.setWidth(!inventoryVisible ? 65 : enchanting != null || alchemy != null ? 94 : cutting || repair != null || upgrade != null || handler instanceof com.cranks.dungeons.screen.LoadoutTailoringHandler || handler instanceof com.cranks.dungeons.screen.LoadoutCartographyHandler ? 83 : 65);
        viewToggle.setX(inventoryVisible ? x + 90 : panelX + panelWidth - 74);
        viewToggle.setY(inventoryVisible ? y - 1 : stationTop + 7);
        viewToggle.setMessage(inventoryVisible && trade!=null ? Text.literal("Trade") : inventoryVisible && creative != null ? Text.literal("Creative") : inventoryVisible && mountPanel != null ? MountPanel.text("title") : inventoryVisible && autoCrafter != null ? Blocks.CRAFTER.getName() : inventoryVisible && storage != null ? StoragePanel.text("view") : inventoryVisible && artificial != null ? com.cranks.dungeons.block.ModBlocks.ARTIFICIAL_ENHANCEMENT_ALTAR.getName()
                : inventoryVisible && runic != null ? com.cranks.dungeons.block.ModBlocks.RUNIC_ENHANCEMENT_ALTAR.getName()
                : inventoryVisible && furnace ? ((LoadoutFurnaceHandler) handler).station().block().getName()
                : inventoryVisible && cutting ? Blocks.STONECUTTER.getName()
                : inventoryVisible && repair != null ? Blocks.GRINDSTONE.getName()
                : inventoryVisible && upgrade != null ? Blocks.SMITHING_TABLE.getName()
                : inventoryVisible && alchemy != null ? Blocks.BREWING_STAND.getName()
                : inventoryVisible && enchanting != null ? Blocks.ENCHANTING_TABLE.getName()
                : inventoryVisible && handler instanceof com.cranks.dungeons.screen.LoadoutFletchingHandler ? Blocks.FLETCHING_TABLE.getName()
                : inventoryVisible && handler instanceof com.cranks.dungeons.screen.LoadoutCartographyHandler ? Blocks.CARTOGRAPHY_TABLE.getName()
                : inventoryVisible && handler instanceof com.cranks.dungeons.screen.LoadoutTailoringHandler ? Blocks.LOOM.getName()
                : inventoryVisible && handler instanceof LoadoutAnvilHandler ? Blocks.ANVIL.getName()
                : MaterialCraftingPanel.message(inventoryVisible ? "tab" : "inventory"));
        if (inventoryVisible && (artificial != null || runic != null || handler instanceof com.cranks.dungeons.screen.LoadoutCartographyHandler)) {
            viewToggle.setWidth(textRenderer.getWidth(viewToggle.getMessage()) + 12);
        }
        if(trade!=null&&!inventoryVisible){viewToggle.setX(panelX+panelWidth-75);viewToggle.setY(trade.top()+trade.height()-24);}
        if(trade!=null)swap.active=!TradeClient.active();
        if(creative!=null&&!inventoryVisible){viewToggle.setX(panelX+panelWidth-75);viewToggle.setY(creative.top()+creative.height()-24);}
        placeSlots();
    }

    public void receiveCraftingResult(CraftMaterialsResultPayload payload) {
        if(trade!=null)return;
        if(creative!=null){creative.receive(payload);return;}
        if (mountPanel != null) return;
        if (autoCrafter != null) { autoCrafter.receive(payload); return; }
        if (storage != null) { storage.receive(payload); return; }
        if (artificial != null) { artificial.receive(payload); return; }
        if (runic != null) { runic.receive(payload); return; }
        if (alchemy != null) {
            alchemy.receive(payload);
            return;
        }
        if (upgrade != null) {
            upgrade.receive(payload);
            return;
        }
        if (repair != null) {
            repair.receive(payload);
            return;
        }
        if (enchanting != null) {
            enchanting.receive(payload);
            return;
        }
        if (salvageRequest != 0 && payload.request() == salvageRequest) {
            salvageRequest = 0;
            crafting.receiveSalvage(payload.result());
            return;
        }
        crafting.receive(payload);
    }

    private void openCompendium() {
        close();
        client.setScreen(new CompendiumScreen(null));
    }

    private void swapArmor() {
        client.interactionManager.clickButton(handler.syncId, PlayerLoadout.SWAP_BUTTON);
    }

    private void placeSlots() {
        if (mountPanel != null) {
            var mount=(com.cranks.dungeons.screen.MountCargoScreenHandler)handler;
            for(int i=mount.saddleSlotIndex();i<mount.cargoStartIndex();i++)place(i,showsCrafting()&&mountPanel.equipmentVisible(i)?mountPanel.equipmentX()-x:-10000,showsCrafting()&&mountPanel.equipmentVisible(i)?mountPanel.equipmentY(i)-y:-10000);
            for(int i=0;i<mount.capacity();i++)place(mount.cargoStartIndex()+i,showsCrafting()&&mountPanel.cargoVisible(i)?mountPanel.cargoX(i)-x:-10000,showsCrafting()&&mountPanel.cargoVisible(i)?mountPanel.cargoY(i)-y:-10000);
        } else if (autoCrafter != null) {
            for(int i=0;i<9;i++)place(com.cranks.dungeons.screen.LoadoutCrafterHandler.MATERIALS+i,showsCrafting()?autoCrafter.slotX(i)-x:-10000,showsCrafting()?autoCrafter.slotY()-y:-10000);
        } else if (storage != null) {
            var container=(com.cranks.dungeons.screen.LoadoutStorageHandler)handler;
            for(int i=0;i<container.storageSize();i++) {
                if(showsCrafting())place(com.cranks.dungeons.screen.LoadoutStorageHandler.STORAGE_START+i,storage.slotX(i)-x,storage.slotY(i)-y);
                else place(com.cranks.dungeons.screen.LoadoutStorageHandler.STORAGE_START+i,-10_000,-10_000);
            }
        } else if (artificial != null) {
            if (showsCrafting()) place(com.cranks.dungeons.screen.ArtificialEnhancementAltarScreenHandler.EQUIPMENT, artificial.slotX() - x, artificial.slotY() - y);
            else place(com.cranks.dungeons.screen.ArtificialEnhancementAltarScreenHandler.EQUIPMENT, -10_000, -10_000);
        } else if (runic != null) {
            for (int i = com.cranks.dungeons.screen.RunicEnhancementAltarScreenHandler.EQUIPMENT; i <= com.cranks.dungeons.screen.RunicEnhancementAltarScreenHandler.OLD_RESULT; i++) {
                if (showsCrafting() && handler.getSlot(i).isEnabled()) place(i, runic.slotX(i) - x, runic.slotY() - y);
                else place(i, -10_000, -10_000);
            }
        } else if (upgrade != null) {
            for (int i = QualityStationScreenHandler.EQUIPMENT; i <= QualityStationScreenHandler.MATERIAL; i++) {
                if (showsCrafting()) place(i, upgrade.slotX(i) - x, upgrade.slotY() - y);
                else place(i, -10_000, -10_000);
            }
        } else if (furnace) {
            for (int i = LoadoutFurnaceHandler.INPUT; i <= LoadoutFurnaceHandler.OUTPUT; i++) {
                if (showsCrafting()) place(i, crafting.slotX(i) - x, crafting.slotY() - y);
                else place(i, -10_000, -10_000);
            }
        }
        if (!showsInventory()) {
            for (int slot = 0; slot < LoadoutStationHandler.PLAYER_SLOT_END; slot++) place(slot, -10_000, -10_000);
            return;
        }
        for (int slot = 0; slot < 5; slot++) place(slot, -10_000, -10_000);
        boolean second = loadout.secondSetActive();
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                int offset = row * 2 + col;
                place(5 + offset, SET_X[second ? 1 : 0] + col * GRID, GEAR_Y + row * GRID);
                place(PlayerLoadout.handlerSlot(PlayerLoadout.SPARE_ARMOR + offset),
                        SET_X[second ? 0 : 1] + col * GRID, GEAR_Y + row * GRID);
            }
        }
        for (int slot = 0; slot < 27; slot++) {
            place(9 + slot, MAIN_X + slot % 9 * GRID, GEAR_Y + slot / 9 * GRID);
        }
        for (int slot = 0; slot < PlayerLoadout.HOTBAR_COUNT; slot++) {
            place(slot == 9 ? PlayerLoadout.FIRST_HANDLER_SLOT : 36 + slot, HOTBAR_X + slot * GRID, HOTBAR_Y);
        }
        place(PlayerScreenHandler.OFFHAND_ID, HOTBAR_X - 27, HOTBAR_Y);
        for (int row = 0; row < PlayerLoadout.BAG_COUNT; row++) {
            place(PlayerLoadout.handlerSlot(PlayerLoadout.EQUIPPED_BAGS + row),
                    FIRST_BAG_SOCKET_X + row * SOCKET_PITCH, BAG_SOCKET_Y);
            for (int column = 0; column < PlayerLoadout.BAG_WIDTH; column++) {
                place(PlayerLoadout.handlerSlot(PlayerLoadout.BAG_STORAGE + row * PlayerLoadout.BAG_WIDTH + column),
                        BAG_X + column * GRID, BAG_Y + row * GRID);
            }
        }
    }

    private void place(int slot, int sx, int sy) {
        ((SlotPositionAccess) handler.getSlot(slot)).cranksdungeons$setPosition(sx, sy);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        placeSlots();
        if (confirmation != null) {
            super.render(context, -10_000, -10_000, delta);
            if (showsStats()) stats.render(context, player, Map.of(), -10_000, -10_000);
            effects.render(context, client);
            drawConfirmation(context, mouseX, mouseY);
            return;
        }
        boolean overStats = showsStats() && stats.contains(mouseX, mouseY);
        boolean overEffects = effects.contains(mouseX, mouseY);
        boolean mountModal=mountPanel!=null&&mountPanel.modalOpen();
        super.render(context, overStats || overEffects || mountModal ? -10_000 : mouseX, overStats || overEffects || mountModal ? -10_000 : mouseY, delta);
        if (showsStats()) {
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 310);
            stats.render(context, player, Map.of(), overEffects || mountModal ? -10_000 : mouseX, overEffects || mountModal ? -10_000 : mouseY);
            context.getMatrices().pop();
        }
        effects.render(context, client);
        if(mountModal){mountPanel.renderModal(context,mouseX,mouseY);return;}
        if(showsInventory())wallet.render(context,textRenderer,x+WALLET_X,y+BAG_SOCKET_Y-3,mouseX,mouseY);
        if (effects.contains(mouseX, mouseY)) {
            effects.tooltip(context, client, mouseX, mouseY);
            return;
        }
        if (overStats) return;
        if(showsInventory()&&wallet.contains(mouseX,mouseY)){wallet.tooltip(context,textRenderer,mouseX,mouseY);return;}
        drawMouseoverTooltip(context, mouseX, mouseY);
        if (!handler.getCursorStack().isEmpty()) return;
        if (overStation(mouseX, mouseY)) {
            if(trade!=null)trade.tooltip(context,mouseX,mouseY);
            else if (creative != null) creative.tooltip(context,mouseX,mouseY);
            else if (mountPanel != null) mountPanel.tooltip(context,mouseX,mouseY);
            else if (autoCrafter != null) autoCrafter.tooltip(context,mouseX,mouseY);
            else if (storage != null) storage.tooltip(context,mouseX,mouseY);
            else if (artificial != null) artificial.tooltip(context, mouseX, mouseY);
            else if (runic != null) runic.tooltip(context, mouseX, mouseY);
            else if (upgrade != null) upgrade.tooltip(context, mouseX, mouseY);
            else if (repair != null) repair.tooltip(context, mouseX, mouseY);
            else if (enchanting != null) enchanting.tooltip(context, mouseX, mouseY);
            else if (alchemy != null) alchemy.tooltip(context, mouseX, mouseY);
            else crafting.tooltip(context, mouseX, mouseY);
            return;
        }
        if (!showsInventory()) return;
        int row = bagRowAt(mouseX, mouseY);
        if (row >= 0 && inventory.getStack(PlayerLoadout.EQUIPPED_BAGS + row).isEmpty()) {
            context.drawTooltip(textRenderer, Text.translatable("inventory.cranks-dungeons.bag_socket"), mouseX, mouseY);
        } else if (mouseX >= x + BAG_X && mouseX < x + BAG_X + PlayerLoadout.BAG_WIDTH * GRID
                && mouseY >= y + BAG_Y && mouseY < y + BAG_Y + PlayerLoadout.BAG_COUNT * GRID) {
            int bag = (mouseY - y - BAG_Y) / GRID;
            int column = (mouseX - x - BAG_X) / GRID;
            Slot storage = handler.getSlot(PlayerLoadout.handlerSlot(PlayerLoadout.BAG_STORAGE + bag * PlayerLoadout.BAG_WIDTH + column));
            if (!storage.isEnabled() && isPointerOverSlot(storage, mouseX, mouseY)) {
                context.drawTooltip(textRenderer, Text.translatable("inventory.cranks-dungeons.bag_locked"), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        if (showsCrafting()) {
            if(trade!=null)trade.render(context,mouseX,mouseY,delta);
            else if (creative != null) creative.render(context,mouseX,mouseY,delta);
            else if (mountPanel != null) mountPanel.render(context,mouseX,mouseY,delta);
            else if (autoCrafter != null) autoCrafter.render(context,mouseX,mouseY,delta);
            else if (storage != null) storage.render(context,mouseX,mouseY,delta);
            else if (artificial != null) artificial.render(context, mouseX, mouseY, delta);
            else if (runic != null) runic.render(context, mouseX, mouseY, delta);
            else if (upgrade != null) upgrade.render(context, mouseX, mouseY);
            else if (repair != null) repair.render(context, mouseX, mouseY);
            else if (enchanting != null) enchanting.render(context, mouseX, mouseY, delta);
            else if (alchemy != null) alchemy.render(context, mouseX, mouseY, delta);
            else crafting.render(context, mouseX, mouseY, delta);
        }
        if (!showsInventory()) return;
        InventoryStyle.drawPanel(context, x + 7, y + BAG_Y - 4, 55,
                PlayerLoadout.BAG_COUNT * GRID + 6, false, Panel.PORTRAIT);
        drawGridFrame(context, BAG_X, BAG_Y, PlayerLoadout.BAG_WIDTH, PlayerLoadout.BAG_COUNT, false, Panel.BAG_STORAGE);
        drawGridFrame(context, MAIN_X, GEAR_Y, 9, 3, false, Panel.STORAGE);
        drawGridFrame(context, HOTBAR_X, HOTBAR_Y, PlayerLoadout.HOTBAR_COUNT, 1, false, Panel.HOTBAR);
        int active = loadout.secondSetActive() ? 1 : 0;
        for (int i = 0; i < SET_X.length; i++) {
            drawGridFrame(context, SET_X[i], GEAR_Y, 2, 2, active == i, Panel.ARMOR);
        }
        for (int index = 5; index < LoadoutStationHandler.PLAYER_SLOT_END; index++) {
            Slot slot = handler.getSlot(index);
            int sx = x + slot.x, sy = y + slot.y;
            if (isSocket(index)) {
                boolean hovered = isPointerOverSlot(slot, mouseX, mouseY);
                if (index >= FIRST_BAG_SOCKET) {
                    InventoryStyle.drawBagSocket(context, sx - 3, sy - 3, SOCKET_SIZE, hovered, index - FIRST_BAG_SOCKET);
                } else {
                    InventoryStyle.drawSocket(context, sx - 3, sy - 3, SOCKET_SIZE, SOCKET_SIZE, hovered);
                }
                context.fill(sx, sy, sx + ITEM_SIZE, sy + ITEM_SIZE, 0xFF1A1A1A);
            } else {
                InventoryStyle.drawCell(context, sx, sy, slot.isEnabled());
            }
            if (slot.hasStack()) continue;
            if (!slot.isEnabled()) {
                InventoryStyle.drawLockIcon(context, sx, sy);
            } else if (index >= FIRST_BAG_SOCKET) {
                InventoryStyle.drawBagIcon(context, sx, sy);
            }
        }

        int hoveredBag = bagRowAt(mouseX, mouseY);
        if (hoveredBag >= 0) {
            int capacity = loadout.capacity(hoveredBag);
            int sy = y + BAG_Y + hoveredBag * GRID;
            int size = InventoryStyle.CELL_SIZE;
            for (int column = 0; column < capacity; column++) {
                int sx = x + BAG_X + column * GRID;
                context.fill(sx, sy, sx + size, sy + size, 0xFF303030);
                context.drawBorder(sx, sy, size, size, 0xFF707070);
            }
        }
        if (blockedBag >= 0 && System.currentTimeMillis() < blockedUntil && loadout.hasBagContents(blockedBag)) {
            for (int column = 0; column < PlayerLoadout.BAG_WIDTH; column++) {
                Slot slot = handler.getSlot(PlayerLoadout.handlerSlot(PlayerLoadout.BAG_STORAGE + blockedBag * PlayerLoadout.BAG_WIDTH + column));
                if (slot.hasStack()) context.drawBorder(x + slot.x - 1, y + slot.y - 1, 18, 18, CompendiumStyle.TEXT_WARNING);
            }
        }
        InventoryScreen.drawEntity(context, x + 10, y + BAG_Y + 2, x + 57, y + BAG_Y + 88,
                29, 0.0625F, mouseX, mouseY, player);
    }

    private void drawGridFrame(DrawContext context, int sx, int sy, int columns, int rows,
                               boolean active, Panel trim) {
        InventoryStyle.drawPanel(context, x + sx - 4, y + sy - 4,
                columns * GRID + 6, rows * GRID + 6, active, trim);
    }

    private static boolean isSocket(int id) {
        return id == PlayerScreenHandler.OFFHAND_ID || id >= FIRST_BAG_SOCKET && id < LoadoutStationHandler.PLAYER_SLOT_END;
    }

    private int slotSize(Slot slot) {
        if ((storage != null || autoCrafter != null || mountPanel != null) && slot.id >= LoadoutStationHandler.PLAYER_SLOT_END) return InventoryStyle.CELL_SIZE;
        return isSocket(slot.id) || slot.id >= LoadoutStationHandler.PLAYER_SLOT_END ? ITEM_SIZE : InventoryStyle.CELL_SIZE;
    }

    public boolean isPointerOverSlot(Slot slot, double mouseX, double mouseY) {
        int size = slotSize(slot);
        int left = x + slot.x;
        int top = y + slot.y;
        boolean visible = slot.id >= LoadoutStationHandler.PLAYER_SLOT_END ? showsCrafting() : showsInventory();
        return visible && slot.id >= PlayerScreenHandler.EQUIPMENT_START
                && mouseX >= left && mouseX < left + size && mouseY >= top && mouseY < top + size;
    }

    public void drawSlotHover(DrawContext context, Slot slot, int z) {
        int size = slotSize(slot);
        int tint = 0x80FFFFFF;
        context.fillGradient(RenderLayer.getGuiOverlay(), slot.x, slot.y, slot.x + size, slot.y + size, tint, tint, z);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        if (showsInventory() && blockedBag >= 0 && System.currentTimeMillis() < blockedUntil && loadout.hasBagContents(blockedBag)) {
            Text warning = Text.translatable("inventory.cranks-dungeons.bag_occupied");
            int textWidth = textRenderer.getWidth(warning);
            int warningX = BAG_X + (PlayerLoadout.BAG_WIDTH * GRID - textWidth) / 2;
            if (compact) warningX = Math.max(warningX, viewToggle.getRight() - x + 8);
            int warningTop = Math.max(-2, 1 - y);
            CompendiumStyle.drawRailPlaque(context, warningX - 6, warningTop,
                    textWidth + 12, textRenderer.fontHeight + 6, false, false);
            context.drawText(textRenderer, warning, warningX, warningTop + 4, CompendiumStyle.TEXT_WARNING, false);
        }
    }

    private int bagRowAt(double mouseX, double mouseY) {
        for (int row = 0; row < PlayerLoadout.BAG_COUNT; row++) {
            Slot slot = handler.getSlot(PlayerLoadout.handlerSlot(PlayerLoadout.EQUIPPED_BAGS + row));
            if (isPointerOverSlot(slot, mouseX, mouseY)) return row;
        }
        return -1;
    }

    @Override
    protected void onMouseClick(Slot slot, int id, int button, SlotActionType action) {
        if(mountPanel!=null&&mountPanel.blockAttachmentRemoval(id,action))return;
        int row = id - PlayerLoadout.handlerSlot(PlayerLoadout.EQUIPPED_BAGS);
        if (row >= 0 && row < PlayerLoadout.BAG_COUNT && action != SlotActionType.QUICK_CRAFT
                && action != SlotActionType.CLONE && loadout.hasBagContents(row)) {
            blockedBag = row;
            blockedUntil = System.currentTimeMillis() + 3600;
            return;
        }
        if (handler instanceof LoadoutFurnaceHandler station && station.isBlastFurnace()) {
            int source = switch (action) {
                case QUICK_MOVE -> id >= 5 && id < LoadoutStationHandler.PLAYER_SLOT_END ? id : -2;
                case PICKUP -> id == LoadoutFurnaceHandler.INPUT ? SalvageGearPayload.CURSOR : -2;
                case SWAP -> id == LoadoutFurnaceHandler.INPUT ? inventoryHandlerSlot(button) : -2;
                default -> -2;
            };
            if (source >= SalvageGearPayload.CURSOR) {
                ItemStack stack = source == SalvageGearPayload.CURSOR ? handler.getCursorStack() : handler.getSlot(source).getStack();
                if (station.canLoadSalvage(stack) && GearSalvage.needsConfirmation(stack)) {
                    requestSalvage(source, stack);
                    return;
                }
            }
        }
        super.onMouseClick(slot, id, button, action);
    }

    private void requestInventorySalvage(int inventorySlot, ItemStack stack) {
        requestSalvage(inventoryHandlerSlot(inventorySlot), stack);
    }

    private int inventoryHandlerSlot(int index) {
        if (index >= 0 && index < 9) return 36 + index;
        if (index >= 9 && index < PlayerInventory.MAIN_SIZE) return index;
        if (index == 40) return PlayerScreenHandler.OFFHAND_ID;
        if (index >= PlayerLoadout.EXTRA_HOTBAR && index < PlayerLoadout.END) return PlayerLoadout.handlerSlot(index);
        return -2;
    }

    private void requestSalvage(int source, ItemStack stack) {
        if (source < SalvageGearPayload.CURSOR || salvageRequest != 0 || confirmation != null) return;
        ItemStack output = GearSalvage.output(stack, furnace);
        if (output.isEmpty()) return;
        ItemStack expected = stack.copy();
        if (GearSalvage.needsConfirmation(stack)) {
            setFocused(null);
            confirmation = new GearConfirmation(expected, output, MaterialCraftingPanel.message("salvage_confirm"),
                    MaterialCraftingPanel.message("salvage_yield", output.getCount(), output.getName()),
                    MaterialCraftingPanel.message("salvage_loss"),
                    MaterialCraftingPanel.message(furnace ? "salvage_load" : "salvage"), () -> sendSalvage(source, expected));
        } else {
            sendSalvage(source, expected);
        }
    }

    private void sendSalvage(int source, ItemStack expected) {
        salvageRequest = MaterialCraftingPanel.nextRequest();
        crafting.startSalvageRequest();
        ClientPlayNetworking.send(new SalvageGearPayload(handler.syncId, salvageRequest, source, expected));
    }

    private void requestRepair(RepairGearPayload request, ItemStack repaired) {
        var donor = request.donor().map(RepairGearPayload.Source::expected).orElse(ItemStack.EMPTY);
        if (GearSalvage.needsConfirmation(donor)) {
            setFocused(null);
            confirmation = new GearConfirmation(donor, repaired, MaterialCraftingPanel.message("repair_confirm"),
                    MaterialCraftingPanel.message("repair_after", EquipmentRepair.remaining(repaired), repaired.getMaxDamage()),
                    MaterialCraftingPanel.message("repair_consumed"), MaterialCraftingPanel.message("repair_action"),
                    () -> repair.send(request));
        } else {
            repair.send(request);
        }
    }

    private void requestArtificial(com.cranks.dungeons.network.ArtificialAltarPayload request) {
        var preview = com.cranks.dungeons.inventory.ArtificialRunes.preview(request.equipment(), request.option());
        var option = com.cranks.dungeons.inventory.ArtificialRunes.options(request.equipment()).stream().filter(row -> row.index() == request.option()).findFirst().orElse(null);
        if (preview.isEmpty() || option == null) return;
        var details = new java.util.ArrayList<Text>();
        for (int i = 0; i < option.stat().getComponents().size(); i++) details.add(Text.literal(
                option.stat().getComponents().get(i).formatValue(option.values()[i], option.stat().getEquipmentType())));
        setFocused(null);
        confirmation = new GearConfirmation(request.equipment(), preview,
                com.cranks.dungeons.inventory.ArtificialRunes.text("confirm_title"),
                com.cranks.dungeons.inventory.ArtificialRunes.text("tier", option.tier()),
                com.cranks.dungeons.inventory.ArtificialRunes.text("warning"),
                com.cranks.dungeons.inventory.ArtificialRunes.text("reveal"), () -> artificial.send(new com.cranks.dungeons.network.ArtificialAltarPayload(
                        request.syncId(), request.request(), request.revision(), request.equipment(), request.option(), true)), details);
    }

    private void requestRunic(com.cranks.dungeons.network.RunicAltarPayload request) {
        if(request.action()!=0){runic.send(request);return;}
        if (!com.cranks.dungeons.inventory.RunicAltarRules.destructive(request.equipment(), request.tome(),request.rune())) { runic.send(request); return; }
        setFocused(null);
        confirmation = new GearConfirmation(request.equipment(), request.tome(),
                com.cranks.dungeons.inventory.RunicAltarRules.text("confirm_title"), request.tome().getName(),
                com.cranks.dungeons.inventory.RunicAltarRules.text("loss_warning"),
                com.cranks.dungeons.inventory.RunicAltarRules.text("apply"), () -> runic.send(new com.cranks.dungeons.network.RunicAltarPayload(
                        request.syncId(), request.request(), request.revision(), request.equipment(), request.tome(), true,request.rune(),request.action())),
                java.util.List.of(com.cranks.dungeons.inventory.RunicAltarRules.text("payment_detail")));
    }

    private void requestEnchant(EnchantGearPayload request, ItemStack enchanted) {
        if (request.removing().isEmpty()) {
            enchanting.send(request);
            return;
        }
        var registry = player.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        boolean replacing = request.enchantment().isPresent();
        var shown = registry.getEntry(request.enchantment().orElseGet(request.removing()::orElseThrow)).orElseThrow();
        int copies = EnchantmentSlotHelper.copyCount(shown, enchanted.getEnchantments().getLevel(shown));
        Text outcome = replacing ? EnchantmentSlotHelper.displayName(shown, copies) : shown.value().description();
        setFocused(null);
        confirmation = new GearConfirmation(request.expected(), enchanted,
                MaterialCraftingPanel.message(replacing ? "enchant_replace_confirm" : "enchant_remove_confirm"),
                outcome, MaterialCraftingPanel.message("enchant_copy_removed"),
                MaterialCraftingPanel.message(replacing ? "enchant_replace" : "enchant_remove"), () -> enchanting.send(request));
    }

    private void dismissConfirmation(boolean confirmed) {
        var action = confirmation;
        confirmation = null;
        if (confirmed) action.confirm.run();
    }

    private int confirmationExtra() { return confirmation.details.stream().mapToInt(value -> textRenderer.wrapLines(value, 184).size() * 10).sum(); }

    private void drawConfirmation(DrawContext context, int mouseX, int mouseY) {
        int left = (width - 212) / 2;
        int extra = confirmationExtra();
        int top = (height - 107 - extra) / 2;
        var action = confirmation;
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 420);
        context.fill(0, 0, width, height, 0x98000000);
        CompendiumStyle.drawPanel(context, left, top, 212, 107 + extra);
        context.drawCenteredTextWithShadow(textRenderer, action.title,
                width / 2, top + 10, CompendiumStyle.TEXT);
        context.drawItem(action.item, left + 12, top + 27);
        context.drawItem(action.output, left + 12, top + 48);
        drawConfirmationLine(context, action.item.getName(), left + 34, top + 30, CompendiumStyle.TEXT);
        drawConfirmationLine(context, action.outcome, left + 34, top + 52, CompendiumStyle.TEXT_SUCCESS);
        int detailY = top + 66;
        for (var detail : action.details) for (var row : textRenderer.wrapLines(detail, 184)) {
            context.drawText(textRenderer, row, left + 14, detailY, CompendiumStyle.TEXT, false); detailY += 10;
        }
        context.drawCenteredTextWithShadow(textRenderer, action.warning,
                width / 2, top + 70 + extra, CompendiumStyle.TEXT_WARNING);
        MaterialCraftingPanel.drawButton(context, MaterialCraftingPanel.message("salvage_cancel"),
                left + 14, top + 84 + extra, 87, 15, mouseX, mouseY, true);
        MaterialCraftingPanel.drawButton(context, action.action,
                left + 111, top + 84 + extra, 87, 15, mouseX, mouseY, true);
        if (MaterialCraftingPanel.inside(mouseX, mouseY, left + 12, top + 27, 16, 16)) {
            context.drawItemTooltip(textRenderer, action.item, mouseX, mouseY);
        } else if (MaterialCraftingPanel.inside(mouseX, mouseY, left + 12, top + 48, 16, 16)) {
            context.drawItemTooltip(textRenderer, action.output, mouseX, mouseY);
        }
        context.getMatrices().pop();
    }

    private void drawConfirmationLine(DrawContext context, Text text, int left, int top, int color) {
        String value = text.getString();
        if (textRenderer.getWidth(value) > 164) value = textRenderer.trimToWidth(value, 158) + "…";
        context.drawText(textRenderer, value, left, top, color, false);
    }

    @Override
    public void setFocused(Element focused) {
        super.setFocused(focused);
        focusedTextField = focused instanceof TextFieldWidget field ? field : null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(mountPanel!=null&&mountPanel.modalOpen()){confirmationClickCaptured=true;mountPanel.modalClick(mouseX,mouseY,button);return true;}
        if (confirmation != null) {
            confirmationClickCaptured = true;
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                int left = (width - 212) / 2, extra = confirmationExtra(), top = (height - 107 - extra) / 2;
                if (MaterialCraftingPanel.inside(mouseX, mouseY, left + 14, top + 84 + extra, 87, 15)) dismissConfirmation(false);
                else if (MaterialCraftingPanel.inside(mouseX, mouseY, left + 111, top + 84 + extra, 87, 15)) dismissConfirmation(true);
            }
            return true;
        }
        if (effects.click(mouseX, mouseY, button)) return true;
        if (viewToggle.visible && viewToggle.mouseClicked(mouseX, mouseY, button)) return true;
        if(showsInventory()&&wallet.contains(mouseX,mouseY)&&button==0){TradeClient.open();return true;}
        if (showsStats() && stats.mouseClicked(mouseX, mouseY, button, this::setFocused)) return true;
        if (overStation(mouseX, mouseY) && !overStationSlot(mouseX, mouseY)) {
            if(trade!=null)trade.click(mouseX,mouseY,button,this::setFocused);
            else if(creative!=null)creative.click(mouseX,mouseY,button,this::setFocused);
            else if (mountPanel != null) {setFocused(null);mountPanel.click(mouseX,mouseY,button);}
            else if (autoCrafter != null) autoCrafter.click(mouseX,mouseY,button,this::setFocused);
            else if (storage != null) storage.click(mouseX,mouseY,button,this::setFocused);
            else if (artificial != null) { setFocused(null); artificial.click(mouseX, mouseY, button); }
            else if (runic != null) runic.click(mouseX, mouseY, button, this::setFocused);
            else if (upgrade != null) {
                setFocused(null);
                upgrade.click(mouseX, mouseY, button);
            } else if (repair != null) repair.click(mouseX, mouseY, button);
            else if (enchanting != null) enchanting.click(mouseX, mouseY, button, this::setFocused);
            else if (alchemy != null) alchemy.click(mouseX, mouseY, button, this::setFocused);
            else crafting.mouseClicked(mouseX, mouseY, button, this::setFocused);
            return true;
        }
        if(trade!=null&&TradeClient.active()&&showsInventory()){
            for(var slot:handler.slots)if(slot.inventory==inventory&&isPointerOverSlot(slot,mouseX,mouseY)){
                setFocused(null);
                if(!TradeClient.pending()&&!TradeClient.state().yourReady()&&!slot.getStack().isEmpty()&&com.cranks.dungeons.economy.TradeInventory.sourceSlot(inventory,slot.getIndex())&&(button==0||button==1))
                    TradeClient.send(com.cranks.dungeons.network.TradeActionPayload.OFFER,slot.getIndex(),button==1?1:slot.getStack().getCount(),0);
                return true;
            }
        }
        if (focusedTextField != null) setFocused(null);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean overStation(double mouseX, double mouseY) {
        return showsCrafting() && (trade!=null?trade.contains(mouseX,mouseY):creative != null ? creative.contains(mouseX,mouseY) : mountPanel != null ? mountPanel.contains(mouseX,mouseY) : autoCrafter != null ? autoCrafter.contains(mouseX,mouseY) : storage != null ? storage.contains(mouseX,mouseY) : artificial != null ? artificial.contains(mouseX, mouseY) : runic != null ? runic.contains(mouseX, mouseY)
                : upgrade != null ? upgrade.contains(mouseX, mouseY)
                : repair != null ? repair.contains(mouseX, mouseY)
                : enchanting != null ? enchanting.contains(mouseX, mouseY)
                : alchemy != null ? alchemy.contains(mouseX, mouseY) : crafting.contains(mouseX, mouseY));
    }

    private boolean overStationSlot(double mouseX, double mouseY) {
        for (int i = LoadoutStationHandler.PLAYER_SLOT_END; i < handler.slots.size(); i++) {
            if (isPointerOverSlot(handler.getSlot(i), mouseX, mouseY)) return true;
        }
        return false;
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        if (effects.contains(mouseX, mouseY)) return false;
        if (overStation(mouseX, mouseY)) return false;
        if (showsStats() && stats.contains(mouseX, mouseY)) return false;
        return super.isClickOutsideBounds(mouseX, mouseY, left, top, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(mountPanel!=null&&mountPanel.modalOpen()){confirmationClickCaptured=false;return true;}
        if (confirmationClickCaptured || confirmation != null) {
            confirmationClickCaptured = false;
            return true;
        }
        return stats.mouseReleased(button) || (trade!=null?false:creative != null ? creative.release(mouseX,mouseY,button) : mountPanel != null ? mountPanel.release(button) : autoCrafter != null ? autoCrafter.release(button) : storage != null ? false : artificial != null ? artificial.release(button) : runic != null ? runic.release(button)
                : upgrade != null ? upgrade.release(button)
                : repair != null ? repair.release(button)
                : enchanting != null ? enchanting.release(button)
                : alchemy != null ? alchemy.release(button) : crafting.mouseReleased(button))
                || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if(mountPanel!=null&&mountPanel.modalOpen())return true;
        return confirmation != null || stats.mouseDragged(mouseY, button)
                || (trade!=null?trade.drag(mouseX,mouseY,button,dx,dy,getFocused()):creative != null ? creative.drag(mouseX,mouseY,button,dx,dy,getFocused()) : mountPanel != null ? mountPanel.drag(mouseY,button) : autoCrafter != null ? autoCrafter.drag(mouseX,mouseY,button,dx,dy,getFocused()) : storage != null ? storage.drag(mouseX,mouseY,button,dx,dy,getFocused()) : artificial != null ? artificial.drag(mouseY, button) : runic != null ? runic.drag(mouseX, mouseY, button, dx, dy, getFocused())
                        : repair != null ? repair.drag(mouseY, button)
                        : enchanting != null ? enchanting.drag(mouseX, mouseY, button, dx, dy, getFocused())
                        : alchemy != null ? alchemy.drag(mouseX, mouseY, button, dx, dy, getFocused()) : crafting != null
                        && crafting.mouseDragged(mouseX, mouseY, button, dx, dy, getFocused()))
                || super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if(mountPanel!=null&&mountPanel.modalOpen()){mountPanel.modalScroll(vertical);return true;}
        if (confirmation != null || effects.scroll(mouseX, mouseY, vertical)
                || showsStats() && stats.mouseScrolled(mouseX, mouseY, vertical)) return true;
        if (overStation(mouseX, mouseY)) {
            if(trade!=null)trade.scroll(vertical);
            else if(creative!=null)creative.scroll(vertical);
            else if (mountPanel != null) mountPanel.scroll(vertical);
            else if (autoCrafter != null) autoCrafter.scroll(mouseX,vertical);
            else if (artificial != null) artificial.scroll(vertical);
            else if (runic != null) runic.scroll(mouseX, vertical);
            else if (repair != null) repair.scroll(mouseX, vertical);
            else if (enchanting != null) enchanting.scroll(mouseX, vertical);
            else if (alchemy != null) alchemy.scroll(mouseX, vertical);
            else if (crafting != null) crafting.mouseScrolled(mouseX, mouseY, vertical);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        if(mountPanel!=null&&mountPanel.modalOpen()){mountPanel.modalKey(key);return true;}
        if (confirmation != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) dismissConfirmation(false);
            return true;
        }
        if (focusedTextField != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) setFocused(null);
            else if((key==GLFW.GLFW_KEY_ENTER||key==GLFW.GLFW_KEY_KP_ENTER)&&trade!=null&&trade.amountFocused(focusedTextField))trade.submitCoins();
            else if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)
                    && crafting != null && crafting.quantityFocused(focusedTextField)) crafting.submit();
            else if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)
                    && alchemy != null && alchemy.quantityFocused(focusedTextField)) alchemy.submit();
            else focusedTextField.keyPressed(key, scan, modifiers);
            return true;
        }
        if(trade!=null&&TradeClient.active()){
            if(client.options.dropKey.matchesKey(key,scan)||ModKeybinds.SWAP_ARMOR.matchesKey(key,scan)||ModKeybinds.TENTH_HOTBAR.matchesKey(key,scan))return true;
            for(var binding:client.options.hotbarKeys)if(binding.matchesKey(key,scan))return true;
        }
        if(creative!=null&&showsCrafting()&&creative.keyPressed(key,scan))return true;
        if (ModKeybinds.SWAP_ARMOR.matchesKey(key, scan)) {
            swapArmor();
            return true;
        }
        if (ModKeybinds.OPEN_COMPENDIUM.matchesKey(key, scan)) {
            openCompendium();
            return true;
        }
        return super.keyPressed(key, scan, modifiers);
    }
}
