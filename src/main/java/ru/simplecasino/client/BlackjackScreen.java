package ru.simplecasino.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import ru.simplecasino.blackjack.BlackjackCards;
import ru.simplecasino.blackjack.BlackjackMenu;
import ru.simplecasino.blackjack.BlackjackTableBlockEntity;
import ru.simplecasino.blackjack.BlackjackTableBlockEntity.Phase;
import ru.simplecasino.blackjack.BlackjackTableBlockEntity.Seat;
import ru.simplecasino.network.BlackjackActionPacket;
import ru.simplecasino.network.ModMessages;

import java.util.List;
import java.util.UUID;

public class BlackjackScreen extends AbstractContainerScreen<BlackjackMenu> {
    private Button btnSit;
    private Button btnLeave;
    private Button btnStart;
    private Button btnNew;
    private Button btnStand;
    private Button btnDeck;

    public BlackjackScreen(BlackjackMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 320;
        this.imageHeight = 200;
        this.inventoryLabelY = 1000;
        this.titleLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft != null) {
            int sw = minecraft.getWindow().getGuiScaledWidth();
            int sh = minecraft.getWindow().getGuiScaledHeight();
            this.imageWidth = Math.min(360, sw - 20);
            this.imageHeight = Math.min(220, sh - 20);
            this.leftPos = (sw - imageWidth) / 2;
            this.topPos = (sh - imageHeight) / 2;
        }

        int x = leftPos;
        int y = topPos;
        int bw = 52;
        int bh = 18;

        btnSit = addRenderableWidget(Button.builder(
                Component.literal("Сесть"),
                b -> ModMessages.sendToServer(new BlackjackActionPacket("join"))
        ).bounds(x + 8, y + imageHeight - 24, bw, bh).build());

        btnLeave = addRenderableWidget(Button.builder(
                Component.literal("Выйти"),
                b -> ModMessages.sendToServer(new BlackjackActionPacket("leave"))
        ).bounds(x + 8 + bw + 4, y + imageHeight - 24, bw, bh).build());

        btnStart = addRenderableWidget(Button.builder(
                Component.literal("Старт"),
                b -> ModMessages.sendToServer(new BlackjackActionPacket("start"))
        ).bounds(x + 8 + 2 * (bw + 4), y + imageHeight - 24, bw, bh).build());

        btnNew = addRenderableWidget(Button.builder(
                Component.literal("Новая"),
                b -> ModMessages.sendToServer(new BlackjackActionPacket("next"))
        ).bounds(x + 8 + 3 * (bw + 4), y + imageHeight - 24, bw, bh).build());

        btnStand = addRenderableWidget(Button.builder(
                Component.literal("Хватит"),
                b -> ModMessages.sendToServer(new BlackjackActionPacket("stand"))
        ).bounds(x + imageWidth - 90, y + imageHeight - 70, 70, 22).build());

        btnDeck = addRenderableWidget(Button.builder(
                Component.literal("Колода"),
                b -> ModMessages.sendToServer(new BlackjackActionPacket("hit"))
        ).bounds(x + 12, y + 50, 56, 70).build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        BlackjackTableBlockEntity table = menu.getTable();
        Phase phase = table.getPhase();
        UUID self = minecraft != null && minecraft.player != null
                ? minecraft.player.getUUID() : null;
        boolean myTurn = self != null && table.isPlayerTurn(self);

        btnSit.visible = phase == Phase.WAITING;
        btnLeave.visible = phase == Phase.WAITING;
        btnStart.visible = phase == Phase.WAITING;
        btnNew.visible = phase == Phase.FINISHED;
        btnStand.visible = phase == Phase.PLAYING && myTurn;
        btnDeck.visible = phase == Phase.PLAYING && myTurn;
        btnDeck.active = myTurn;
        btnStand.active = myTurn;
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = leftPos;
        int y = topPos;

        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF000000);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF1B5E20);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + 22, 0xFF0D3B0D);

        int colX = x + imageWidth - 78;
        g.fill(colX, y + 22, x + imageWidth - 2, y + imageHeight - 2, 0xFF0A2A0A);
        g.fill(colX, y + 22, colX + 1, y + imageHeight - 2, 0xFF000000);

        int dx = x + 12;
        int dy = y + 50;
        g.fill(dx, dy, dx + 56, dy + 70, 0xFF4A0000);
        g.fill(dx + 2, dy + 2, dx + 54, dy + 68, 0xFF8B0000);
        g.drawCenteredString(font, "КОЛОДА", dx + 28, dy + 30, 0xFFFFFF);
        g.drawCenteredString(font, "взять", dx + 28, dy + 42, 0xFFCCCCCC);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        BlackjackTableBlockEntity table = menu.getTable();
        UUID self = minecraft != null && minecraft.player != null
                ? minecraft.player.getUUID() : null;

        g.drawString(font, "БЛЭКДЖЕК", 6, 7, 0xFFFFFF, false);

        Seat mySeat = self == null ? null : table.getSeat(self);
        int myBet = mySeat == null ? 0 : mySeat.betCount;
        int pot = table.getPot();
        Item potItem = table.getPotItem();
        String itemName = "—";
        if (potItem != null) {
            itemName = new ItemStack(potItem).getHoverName().getString();
        }

        String top = "СТАВКА " + myBet
                + "   БАНК " + pot
                + "   ПРЕДМЕТ " + itemName;
        g.drawString(font, top, 80, 7, 0xFFE0E0E0, false);

        if (table.getPhase() == Phase.PLAYING) {
            Seat turn = table.getTurnSeat();
            if (turn != null) {
                g.drawString(font, "Ход: " + turn.name, 6, 26, 0xFFFFFF55, false);
            }
        }

        int colTextX = imageWidth - 72;
        g.drawString(font, "Игроки", colTextX, 26, 0xFFAAFFAA, false);
        List<Seat> seats = table.getSeats();
        int ly = 40;
        for (int i = 0; i < seats.size(); i++) {
            Seat s = seats.get(i);
            boolean turn = table.getPhase() == Phase.PLAYING
                    && table.getTurnIndex() == i;
            String line = (turn ? "> " : "  ") + s.name;
            int color = turn ? 0xFFFFFF55 : 0xFFFFFFFF;
            if (s.bust) {
                line += " X";
                color = 0xFFFF6666;
            } else if (s.stood) {
                line += " OK";
            }
            g.drawString(
                    font,
                    font.plainSubstrByWidth(line, 68),
                    colTextX,
                    ly,
                    color,
                    false);
            ly += 12;
        }

        int ox = 80;
        int oy = 40;
        for (Seat s : seats) {
            if (self != null && s.id.equals(self)) {
                continue;
            }
            int n = s.hand.size();
            for (int c = 0; c < n; c++) {
                drawCardBack(g, ox + c * 10, oy, 14, 18);
            }
            if (n > 0) {
                g.drawString(font, s.name, ox, oy + 20, 0xFFCCCCCC, false);
            }
            ox += 50;
            if (ox > imageWidth - 120) {
                ox = 80;
                oy += 36;
            }
        }

        if (mySeat != null && !mySeat.hand.isEmpty()) {
            int cx = 80;
            int cy = imageHeight - 100;
            for (int i = 0; i < mySeat.hand.size(); i++) {
                drawCardFace(g, cx + i * 36, cy, mySeat.hand.get(i));
            }
            int score = BlackjackCards.score(mySeat.hand);
            String sc = "Очки: " + score + (mySeat.bust ? " ПЕРЕБОР" : "");
            g.drawString(
                    font,
                    sc,
                    cx,
                    cy + 48,
                    mySeat.bust ? 0xFFFF5555 : 0xFFFFFFFF,
                    false);
        }

        if (table.getPhase() == Phase.FINISHED
                && table.getLastResult() != null
                && !table.getLastResult().isEmpty()) {
            g.drawString(
                    font,
                    table.getLastResult(),
                    80,
                    imageHeight - 40,
                    0xFF55FF55,
                    false);
        }

        if (table.getPhase() == Phase.WAITING) {
            g.drawString(
                    font,
                    "Ставка = стак в руке. 2+ игрока. Один тип предмета.",
                    80,
                    imageHeight - 42,
                    0xFFAAAAAA,
                    false);
        }
    }

    private void drawCardBack(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xFF000000);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF8B0000);
        g.fill(x + 3, y + 3, x + w - 3, y + h - 3, 0xFFCC2222);
    }

    private void drawCardFace(GuiGraphics g, int x, int y, int card) {
        int w = 32;
        int h = 44;
        g.fill(x, y, x + w, y + h, 0xFF000000);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFFFFFF0);
        String name = BlackjackCards.cardName(card);
        g.drawCenteredString(font, name, x + w / 2, y + h / 2 - 4, 0xFF202020);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        super.render(g, mx, my, pt);
        renderTooltip(g, mx, my);
    }
}
