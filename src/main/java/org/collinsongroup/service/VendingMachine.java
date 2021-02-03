package org.collinsongroup.service;

import org.collinsongroup.bean.Bucket;
import org.collinsongroup.bean.Coin;
import org.collinsongroup.bean.Inventory;
import org.collinsongroup.bean.Item;
import org.collinsongroup.exception.NotFullPaidException;
import org.collinsongroup.exception.NotSufficientChangeException;
import org.collinsongroup.exception.SoldOutException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VendingMachine {
  private final Inventory<Coin> cashInventory;
  private final Inventory<Item> itemInventory;
  private long totalSales;
  private Item currentItem;
  private long currentBalance;

  public VendingMachine(Inventory<Coin> cashInventory, Inventory<Item> itemInventory) {
    this.cashInventory = cashInventory;
    this.itemInventory = itemInventory;
    initialize();
  }

  public long selectItemAndGetPrice(Item item) {
    if (item == null) {
      throw new IllegalArgumentException("Please provide a valid Item to buy");
    }

    if (itemInventory.hasItem(item)) {
      currentItem = item;
      return currentItem.getPrice();
    }
    throw new SoldOutException("Sold Out, Please buy another item");
  }

  public void insertCoin(Coin coin) {
    currentBalance = currentBalance + coin.getDenomination();
    cashInventory.add(coin);
  }

  public Bucket<Item, List<Coin>> collectItemAndChange() {
    System.out.println("before collect");
    Item item = collectItem();
    System.out.println("after collect");
    totalSales = totalSales + currentItem.getPrice();

    List<Coin> change = collectChange();

    return new Bucket<>(item, change);
  }

  public List<Coin> refund() {
    List<Coin> refund = getChange(currentBalance);
    updateCashInventory(refund);
    currentBalance = 0;
    currentItem = null;
    return refund;
  }

  public void reset() {
    cashInventory.clear();
    itemInventory.clear();
    totalSales = 0;
    currentItem = null;
    currentBalance = 0;
  }

  public void printStats() {
    System.out.println("Total Sales : " + totalSales);
    System.out.println("Current Item Inventory : " + itemInventory);
    System.out.println("Current Cash Inventory : " + cashInventory);
  }

  public void setTotalSales(long totalSales) {
    this.totalSales = totalSales;
  }

  public Item getCurrentItem() {
    return currentItem;
  }

  public void setCurrentItem(Item currentItem) {
    this.currentItem = currentItem;
  }

  public long getCurrentBalance() {
    return currentBalance;
  }

  public void setCurrentBalance(long currentBalance) {
    this.currentBalance = currentBalance;
  }

  private Item collectItem() throws NotSufficientChangeException,
      NotFullPaidException {
    if (isFullPaid()) {
      if (hasSufficientChange()) {
        System.out.println("deducting --------- " + currentItem);
        itemInventory.deduct(currentItem);
        return currentItem;
      }
      throw new NotSufficientChangeException("Not Sufficient change in Inventory");

    }
    long remainingBalance = currentItem.getPrice() - currentBalance;
    throw new NotFullPaidException("Price not full paid, remaining : ",
        remainingBalance);
  }

  private List<Coin> collectChange() {
    long changeAmount = currentBalance - currentItem.getPrice();
    List<Coin> change = getChange(changeAmount);
    updateCashInventory(change);
    currentBalance = 0;
    currentItem = null;
    return change;
  }

  private boolean hasSufficientChange() {
    return hasSufficientChangeForAmount(currentBalance - currentItem.getPrice());
  }

  private boolean isFullPaid() {
    return currentBalance >= currentItem.getPrice();
  }

  private List<Coin> getChange(long amount) throws NotSufficientChangeException {
    List<Coin> changes = Collections.EMPTY_LIST;

    if (amount > 0) {
      changes = new ArrayList<>();
      long balance = amount;
      while (balance > 0) {
        if (balance >= Coin.QUARTER.getDenomination()
            && cashInventory.hasItem(Coin.QUARTER)) {
          changes.add(Coin.QUARTER);
          balance = balance - Coin.QUARTER.getDenomination();
        } else if (balance >= Coin.DIME.getDenomination()
            && cashInventory.hasItem(Coin.DIME)) {
          changes.add(Coin.DIME);
          balance = balance - Coin.DIME.getDenomination();
        } else if (balance >= Coin.NICKLE.getDenomination()
            && cashInventory.hasItem(Coin.NICKLE)) {
          changes.add(Coin.NICKLE);
          balance = balance - Coin.NICKLE.getDenomination();
        } else if (balance >= Coin.PENNY.getDenomination()
            && cashInventory.hasItem(Coin.PENNY)) {
          changes.add(Coin.PENNY);
          balance = balance - Coin.PENNY.getDenomination();
        } else {
          throw new NotSufficientChangeException("Not Sufficient change in Inventory, Please buy another product");
        }
      }
    }

    return changes;
  }

  private boolean hasSufficientChangeForAmount(long amount) {
    try {
      getChange(amount);
    } catch (NotSufficientChangeException e) {
      return false;
    }

    return true;
  }

  private void updateCashInventory(List<Coin> change) {
    for (Coin c : change) {
      cashInventory.deduct(c);
    }
  }

  public long getTotalSales() {
    return totalSales;
  }

  private void initialize() {
    //initialize machine with 5 coins of each denomination
    //and 5 cans of each Item
    for (Coin c : Coin.values()) {
      cashInventory.put(c, 5);
    }

    for (Item i : Item.values()) {
      itemInventory.put(i, 5);
    }

  }

}
