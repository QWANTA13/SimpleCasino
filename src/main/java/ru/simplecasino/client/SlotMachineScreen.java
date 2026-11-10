package ru.simplecasino.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import ru.simplecasino.SimpleCasino;
import ru.simplecasino.menu.SlotMachineMenu;
import ru.simplecasino.menu.SlotSymbols;
import ru.simplecasino.network.CycleBetPacket;
import ru.simplecasino.network.ModMessages;
import ru.simplecasino.network.SpinPacket;

public class SlotMachineScreen extends AbstractContainerScreen<SlotMachineMenu> {
    private static final int ANIM_TICKS = 35;

    private static final ResourceLocation[] SYMBOL_TEX =
            new ResourceLocation[SlotSymbols.COUNT];

    static {
        for (int i = 0; i < SlotSymbols.COUNT; i++) {
            SYMBOL_TEX[i] = ResourceLocation.fromNamespaceAndPath(
                    SimpleCasino.MOD_ID,
                    "textures/gui/symbol_" + SlotSymbols.NAMES[i] + ".png");
        }
    }

    private int animTicksLeft = 0;
    private final int[] animReels = new int[3];
    private long lastAnimMs = 0;

    public SlotMachineScreen(
            SlotMachineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 150;
        this.inventoryLabelY = 1000;
        this.titleLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos;

        addRenderableWidget(Button.builder(
                Component.translatable("gui.simplecasino.spin"),
                b -> {
                    if (animTicksLeft > 0) {
                        return;
                    }
                    ModMessages.sendToServer(new SpinPacket());
                    startAnimation();
                }
        ).bounds(x + 38, y + 78, 100, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.simplecasino.make_bet"),
                b -> {
                    if (animTicksLeft > 0) {
                        return;
                    }
                    ModMessages.sendToServer(new CycleBetPacket());
                }
        ).bounds(x + 28, y + 104, 120, 20).build());
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
        if (animTicksLeft <= 0) {
            return;
        }
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

    private void drawSymbol(GuiGraphics g, int symbol, int rx, int baseY, int size) {
        symbol = Mth.clamp(symbol, 0, SlotSymbols.COUNT - 1);
        int tex = 16;
        int ox = rx + (size - tex) / 2;
        int oy = baseY + (size - tex) / 2;
        g.blit(SYMBOL_TEX[symbol], ox, oy, 0, 0, tex, tex, tex, tex);
    }

    @Override
    protected void renderBg(
            GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1A1A1A);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFFF5F5F5);
        g.fill(x + 3, y + 3, x + imageWidth - 3, y + 4, 0xFF000000);
        g.fill(x + 3, y + imageHeight - 4, x + imageWidth - 3, y + imageHeight - 3, 0xFF000000);
        g.fill(x + 3, y + 3, x + 4, y + imageHeight - 3, 0xFF000000);
        g.fill(x + imageWidth - 4, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xFF000000);

        int baseX = x + 28;
        int baseY = y + 28;
        int size = 36;
        int gap = 8;
        for (int i = 0; i < 3; i++) {
            int rx = baseX + i * (size + gap);
            g.fill(rx - 1, baseY - 1, rx + size + 1, baseY + size + 1, 0xFF000000);
            g.fill(rx, baseY, rx + size, baseY + size, 0xFFFFFFFF);

            int symbol;
            if (animTicksLeft > 0) {
                symbol = animReels[i];
            } else {
                symbol = menu.getReel(i);
            }
            drawSymbol(g, symbol, rx, baseY, size);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 8, 0x000000, false);

        String betLabel = Component.translatable(
                "gui.simplecasino.bet_label").getString();
        int betShow = menu.getBetAmount();
        if (menu.isBetAll() && this.minecraft != null
                && this.minecraft.player != null) {
            betShow = menu.getBetAmount(this.minecraft.player);
        }
        String betLine = betLabel + " " + betShow;
        int betW = font.width(betLine);
        g.drawString(font, betLine, imageWidth - 10 - betW, 8, 0x000000, false);

        if (animTicksLeft == 0) {
            int win = menu.getLastWin();
            if (win > 0) {
                String winStr = Component.translatable(
                        "gui.simplecasino.win", win).getString();
                int ww = font.width(winStr);
                g.drawString(
                        font, winStr, imageWidth / 2 - ww / 2, 128, 0x006600, false);
            }
        }
    }

    @Override
    public void render(
            GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
