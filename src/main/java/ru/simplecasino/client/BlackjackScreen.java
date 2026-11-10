package ru.simplecasino.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import ru.simplecasino.blackjack.BlackjackCards;
import ru.simplecasino.blackjack.BlackjackMenu;
import ru.simplecasino.blackjack.BlackjackTableBlockEntity;
import ru.simplecasino.blackjack.BlackjackTableBlockEntity.Phase;
import ru.simplecasino.blackjack.BlackjackTableBlockEntity.Seat;
import ru.simplecasino.network.BlackjackActionPacket;
import ru.simplecasino.network.ModMessages;

import java.util.List;

public class BlackjackScreen extends AbstractContainerScreen<BlackjackMenu> {
    public BlackjackScreen(BlackjackMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 220;
        this.imageHeight = 160;
        this.inventoryLabelY = 1000;
        this.titleLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos;
        int bw = 60;
        int bh = 18;

        addRenderableWidget(Button.builder(Component.literal("Сесть"), b ->
                ModMessages.sendToServer(new BlackjackActionPacket("join"))
        ).bounds(x + 8, y + 130, bw, bh).build());

        addRenderableWidget(Button.builder(Component.literal("Выйти"), b ->
                ModMessages.sendToServer(new BlackjackActionPacket("leave"))
        ).bounds(x + 72, y + 130, bw, bh).build());

        addRenderableWidget(Button.builder(Component.literal("Старт"), b ->
                ModMessages.sendToServer(new BlackjackActionPacket("start"))
        ).bounds(x + 136, y + 130, bw, bh).build());

        addRenderableWidget(Button.builder(Component.literal("Ещё"), b ->
                ModMessages.sendToServer(new BlackjackActionPacket("hit"))
        ).bounds(x + 8, y + 108, bw, bh).build());

        addRenderableWidget(Button.builder(Component.literal("Хватит"), b ->
                ModMessages.sendToServer(new BlackjackActionPacket("stand"))
        ).bounds(x + 72, y + 108, bw, bh).build());

        addRenderableWidget(Button.builder(Component.literal("Новая"), b ->
                ModMessages.sendToServer(new BlackjackActionPacket("next"))
        ).bounds(x + 136, y + 108, bw, bh).build());
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1A1A1A);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF0B3D0B);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        BlackjackTableBlockEntity table = menu.getTable();
        g.drawString(font, "Блэкджек (макс 4)", 8, 6, 0xFFFFFF, false);
        g.drawString(font, "Фаза: " + table.getPhase().name(), 8, 18, 0xCCCCCC, false);

        List<Seat> seats = table.getSeats();
        int line = 32;
        for (int i = 0; i < seats.size(); i++) {
            Seat s = seats.get(i);
            boolean turn = table.getPhase() == Phase.PLAYING && table.getTurnIndex() == i;
            StringBuilder sb = new StringBuilder();
            if (turn) sb.append("► ");
            sb.append(s.name).append(" [").append(s.betCount).append("] ");
            if (!s.hand.isEmpty()) {
                for (int c : s.hand) {
                    sb.append(BlackjackCards.cardName(c)).append(" ");
                }
                sb.append("= ").append(BlackjackCards.score(s.hand));
                if (s.bust) sb.append(" ПЕРЕБОР");
                else if (s.stood) sb.append(" (стоп)");
            }
            g.drawString(font, sb.toString(), 8, line, turn ? 0xFFFF55 : 0xFFFFFF, false);
            line += 12;
        }

        if (table.getPhase() == Phase.WAITING) {
            g.drawString(font, "Ставка = весь стак в руке. Нужно 2+ игрока.", 8, 96, 0xAAAAAA, false);
        }
        if (table.getPhase() == Phase.FINISHED && table.getLastResult() != null) {
            g.drawString(font, table.getLastResult(), 8, 96, 0x55FF55, false);
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        super.render(g, mx, my, pt);
        renderTooltip(g, mx, my);
    }
}
