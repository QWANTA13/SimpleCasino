package ru.simplecasino.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import ru.simplecasino.menu.SlotMachineMenu;
import ru.simplecasino.menu.SlotSymbols;
import ru.simplecasino.network.CycleBetPacket;
import ru.simplecasino.network.ModMessages;
import ru.simplecasino.network.SpinPacket;

public class SlotMachineScreen extends AbstractContainerScreen<SlotMachineMenu> {
    private static final int ANIM_TICKS = 35;

    private static final int[] SYMBOL_COLORS = {
            0xFFE03040,
            0xFFF0D030,
            0xFFFF3030,
            0xFF50C8FF,
            0xFFFFC828,
    };
    private static final String[] SYMBOL_LABELS = {"C", "L", "7", "D", "B"};

    private int animTicksLeft = 0;
    private final int[] animReels = new int[3];
    private long lastAnimMs = 0;

    public SlotMachineScreen(SlotMachineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 100;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos;

        addRenderableWidget(Button.builder(Component.translatable("gui.simplecasino.bet"), b -> {
            if (animTicksLeft > 0) return;
            ModMessages.sendToServer(new CycleBetPacket());
        }).bounds(x + 12, y + 70, 50, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.simplecasino.spin"), b -> {
            if (animTicksLeft > 0) return;
            ModMessages.sendToServer(new SpinPacket());
            startAnimation();
        }).bounds(x + 70, y + 70, 60, 20).build());
    }

    private void startAnimation() {
        animTicksLeft = ANIM_TICKS;
        lastAnimMs = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            animReels[i] = menu.getReel(i);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (animTicksLeft > 0) {
            animTicksLeft--;
            long now = System.currentTimeMillis();
            if (now - lastAnimMs > 50) {
                lastAnimMs = now;
                for (int i = 0; i < 3; i++) {
                    int stopAt = ANIM_TICKS - (i + 1) * 8;
                    if (animTicksLeft > stopAt) {
                        animReels[i] = (animReels[i] + 1 + i) % SlotSymbols.COUNT;
                    } else {
                        animReels[i] = menu.getReel(i);
                    }
                }
            }
            if (animTicksLeft == 0) {
                for (int i = 0; i < 3; i++) {
                    animReels[i] = menu.getReel(i);
                }
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        g.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFF8B8B8B);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFFC6C6C6);

        g.fill(x + 10, y + 16, x + imageWidth - 10, y + 62, 0xFF1A1A28);

        int baseX = x + 34;
        int baseY = y + 22;
        for (int i = 0; i < 3; i++) {
            int rx = baseX + i * 38;
            g.fill(rx - 1, baseY - 1, rx + 33, baseY + 33, 0xFF000000);
            g.fill(rx, baseY, rx + 32, baseY + 32, 0xFF0A0A12);

            int symbol = animTicksLeft > 0 ? animReels[i] : menu.getReel(i);
            symbol = Mth.clamp(symbol, 0, SlotSymbols.COUNT - 1);

            int pad = 6;
            g.fill(rx + pad, baseY + pad, rx + 32 - pad, baseY + 32 - pad, SYMBOL_COLORS[symbol]);
            String label = SYMBOL_LABELS[symbol];
            int tw = font.width(label);
            g.drawString(font, label, rx + 16 - tw / 2, baseY + 12, 0xFF000000, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0x404040, false);

        String betText = Component.translatable("gui.simplecasino.bet_value", menu.getBetAmount()).getString();
        g.drawString(font, betText, imageWidth - 8 - font.width(betText), 75, 0x203020, false);

        if (animTicksLeft == 0) {
            int win = menu.getLastWin();
            if (win > 0) {
                g.drawString(font,
                        Component.translatable("gui.simplecasino.win", win),
                        8, 75, 0x006600, false);
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
