package whosalbercik.ccashexchange.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;
import whosalbercik.ccashexchange.core.ModMenus;
import whosalbercik.ccashexchange.object.AskTransaction;
import whosalbercik.ccashexchange.object.BidTransaction;
import whosalbercik.ccashexchange.object.Transaction;

import java.util.*;

public class ItemMarketMenu extends ChestMenu {

    private Item item;

    public ItemMarketMenu(int i, Inventory inventory) {
        super(ModMenus.ITEM_MARKET_MENU.get(), i, inventory,new SimpleContainer(54),  6);
    }

    public Item getItem() {
        return item;
    }

    public ItemMarketMenu(int i, Inventory inventory, Item item) {
        super(ModMenus.ITEM_MARKET_MENU.get(), i, inventory,new SimpleContainer(54),  6);
        this.item = item;
    }


    public boolean addTransactions(ArrayList<Transaction> transactions, int page) {
        decor();

        // page buttons
        SimpleContainer container = (SimpleContainer) this.getContainer();
        ItemStack pageDown = new ItemStack(Items.RED_WOOL);
        pageDown.getOrCreateTag().put("ccash.pagedown", StringTag.valueOf("true"));
        pageDown.getOrCreateTag().put("ccash.page", IntTag.valueOf(page));
        pageDown.getOrCreateTag().put("ccash.gui", StringTag.valueOf("true"));
        pageDown.setHoverName(Component.literal("PREVIOUS PAGE").withStyle(ChatFormatting.RED));
        pageDown.getOrCreateTag().put("ccash.item", StringTag.valueOf(ForgeRegistries.ITEMS.getResourceKey(item).get().location().toString()));


        ItemStack pageUp = new ItemStack(Items.GREEN_WOOL);
        pageUp.getOrCreateTag().put("ccash.pageup", StringTag.valueOf("true"));
        pageUp.getOrCreateTag().put("ccash.page", IntTag.valueOf(page));
        pageUp.getOrCreateTag().put("ccash.gui", StringTag.valueOf("true"));
        pageUp.setHoverName(Component.literal("NEXT PAGE").withStyle(ChatFormatting.GREEN));
        pageUp.getOrCreateTag().put("ccash.item", StringTag.valueOf(ForgeRegistries.ITEMS.getResourceKey(item).get().location().toString()));

        ItemStack pageIndicator = new ItemStack(Items.BLUE_STAINED_GLASS);
        pageIndicator.setHoverName(Component.literal("PAGE NR. " + String.valueOf(page)).withStyle(ChatFormatting.DARK_AQUA));

        container.setItem(4, pageIndicator);
        container.setItem(1, pageDown);
        container.setItem(7, pageUp);


        // transactions of the correct item
        ArrayList<Transaction>  correctTransactions = new ArrayList<Transaction>();

        transactions.forEach((transaction) -> {if (transaction.getItemstack().getItem().equals(item))  correctTransactions.add(transaction);});

        // no transactions in this page
        if (correctTransactions.size() <= 28 * (page - 1)) {
            return false;
        }

        // transactions that should be on the specified page

        ArrayList<Transaction> sorted = sort(correctTransactions);

        List<Transaction> pagedTransactions = sorted.subList(28 * (page - 1), Math.min(correctTransactions.size(), 28 * page));

        pagedTransactions.add(0, null);
        pagedTransactions.add(Math.min(8, pagedTransactions.size()), null);
        pagedTransactions.add((Math.min(9, pagedTransactions.size())), null);
        pagedTransactions.add((Math.min(17, pagedTransactions.size())), null);
        pagedTransactions.add((Math.min(18, pagedTransactions.size())), null);
        pagedTransactions.add((Math.min(26, pagedTransactions.size())), null);
        pagedTransactions.add((Math.min(27, pagedTransactions.size())), null);
        pagedTransactions.add((Math.min(35, pagedTransactions.size())), null);
        pagedTransactions.add((Math.min(36, pagedTransactions.size())), null);
        pagedTransactions.add((Math.min(53, pagedTransactions.size())), null);


        for (Transaction transaction: pagedTransactions) {
            
            if (transaction == null) {
                container.setItem(9 + pagedTransactions.indexOf(transaction), new ItemStack(Items.AIR));
                continue;
            }
            
            ItemStack icona = new ItemStack(transaction.getItemstack().getItem());
            icona.setHoverName(Component.literal((String.format("[%s] %sx for $%s (total $%s)",
                    transaction instanceof BidTransaction ? "BID" : "ASK",
                    transaction.getItemstack().getCount(),
                    transaction.getPrice(),
                    transaction.getPrice() * transaction.getItemstack().getCount())))
                    .withStyle(ChatFormatting.UNDERLINE).withStyle(ChatFormatting.GREEN));

            icona.getOrCreateTag().put("ccash.gui", StringTag.valueOf("true"));
            icona.getTag().put("ccash.id", IntTag.valueOf(transaction.getId()));
            icona.getTag().put("ccash.type", StringTag.valueOf(transaction instanceof BidTransaction ? "bid" : "ask"));



            container.setItem(9 + pagedTransactions.indexOf(transaction), icona.copy());
        }

        return true;
    }

    private ArrayList<Transaction> sort(List<Transaction> transactions) {
        ArrayList<BidTransaction> bids = new ArrayList<BidTransaction>();
        ArrayList<AskTransaction> asks = new ArrayList<AskTransaction>();

        // get bids
        for (Transaction transaction: transactions) {
            if (transaction instanceof BidTransaction) bids.add((BidTransaction) transaction);
            else asks.add((AskTransaction) transaction);
        }

        BidTransaction[] bidArray = bids.toArray(BidTransaction[]::new);

        Arrays.sort(bidArray);

        AskTransaction[] askArray = asks.toArray(AskTransaction[]::new);

        Arrays.sort(askArray);

        ArrayList<Transaction> all = new ArrayList<Transaction>();


        // mixing
        if (bids.size() < asks.size()) {
            for (BidTransaction bid: bidArray) {
                all.add(bid);
                all.add(askArray[ArrayUtils.indexOf(bidArray, bid)]);

                askArray = ArrayUtils.remove(askArray, ArrayUtils.indexOf(bidArray, bid));
                bidArray = ArrayUtils.removeElement(bidArray, bid);
            }
            all.addAll(List.of(askArray));
        } else {
            for (AskTransaction ask: askArray) {
                all.add(ask);
                all.add(bidArray[ArrayUtils.indexOf(askArray, ask)]);

                bidArray = ArrayUtils.remove(bidArray, ArrayUtils.indexOf(askArray, ask));
                askArray = ArrayUtils.removeElement(askArray, ask);
            }
            all.addAll(List.of(bidArray));
        }
        return all;
      }

    private void decor() {
        SimpleContainer container = (SimpleContainer) this.getContainer();
        // decor
        container.setItem(0, new ItemStack(Items.PINK_STAINED_GLASS_PANE));
        container.setItem(8, new ItemStack(Items.PINK_STAINED_GLASS_PANE));



        container.setItem(45, new ItemStack(Items.PINK_STAINED_GLASS_PANE));
        container.setItem(53, new ItemStack(Items.PINK_STAINED_GLASS_PANE));
    }
}
