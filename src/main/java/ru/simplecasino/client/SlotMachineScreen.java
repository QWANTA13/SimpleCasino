package ru.simplecasino.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import ru.simplecasino.SimpleCasino;
import ru.simplecasino.menu.SlotMachineMenu;
import ru.simplecasino.menu.SlotSymbols;
import ru.simplecasino.network.CycleBetPacket;
import ru.simplecasino.network.ModMessages;
import ru.simplecasino.network.SpinPacket;

public class SlotMachineScreen extends AbstractContainerScreen<SlotMachineMenu> {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(SimpleCasino.MOD_ID, "textures/gui/slot_machine.png");

    public SlotMachineScreen(SlotMachineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos;

        addRenderableWidget(Button.builder(Component.translatable("gui.simplecasino.spin"), b ->
                        ModMessages.sendToServer(new SpinPacket()))
                .bounds(x + 60, y + 58, 56, 20)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("gui.simplecasino.bet"), b ->
                        ModMessages.sendToServer(new CycleBetPacket()))
                .bounds(x + 10, y + 58, 44, 20)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // fallback dark panel if texture missing
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF1A1A2E);
        g.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + 78, 0xFF16213E);

        // three reel windows
        int baseX = leftPos + 40;
        int baseY = topPos + 20;
        for (int i = 0; i < 3; i++) {
            int rx = baseX + i * 36;
            g.fill(rx, baseY, rx + 32, baseY + 32, 0xFF0F0F1A);
            int symbol = menu.getReel(i);
            String name = SlotSymbols.NAMES[Math.floorMod(symbol, SlotSymbols.COUNT)];
            g.drawCenteredString(font, name.substring(0, Math.min(3, name.length())).toUpperCase(),
                    rx + 16, baseY + 12, 0xFFFFD700);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0xFFFFFF, false);
        g.drawString(font,
                Component.translatable("gui.simplecasino.bet_value", menu.getBetAmount()),
                100, 62, 0x55FF55, false);
        int win = menu.getLastWin();
        if (win > 0) {
            g.drawString(font,
                    Component.translatable("gui.simplecasino.win", win),
                    8, 48, 0x00FF88, false);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
