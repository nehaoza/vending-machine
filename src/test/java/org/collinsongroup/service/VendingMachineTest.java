package org.collinsongroup.service;

import org.collinsongroup.bean.Bucket;
import org.collinsongroup.bean.Coin;
import org.collinsongroup.bean.Inventory;
import org.collinsongroup.bean.Item;
import org.collinsongroup.exception.NotFullPaidException;
import org.collinsongroup.exception.NotSufficientChangeException;
import org.collinsongroup.exception.SoldOutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendingMachineTest {

  @Mock
  VendingMachine vm;

  @Mock
  Inventory<Coin> cashInventory;

  @Mock
  Inventory<Item> itemInventory;

  @BeforeEach
  void init() {
    vm = new VendingMachine(cashInventory, itemInventory);
  }

  @Test
  @DisplayName(value = "hello")
  void selectItemAndGetPrice_withInvalidItem() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> vm.selectItemAndGetPrice(null));
    assertEquals("Please provide a valid Item to buy", exception.getMessage());
  }

  @Test
  void selectItemAndGetPrice_whenItemIsNotAvailable() {
    when(itemInventory.hasItem(Item.COKE)).thenReturn(false);

    SoldOutException exception = assertThrows(SoldOutException.class, () -> vm.selectItemAndGetPrice(Item.COKE));
    assertEquals("Sold Out, Please buy another item", exception.getMessage());

    verify(itemInventory, times(1)).hasItem(any());
  }

  @ParameterizedTest
  @EnumSource(Item.class)
  void selectItemAndGetPrice_withValidItem(Item item) {
    when(itemInventory.hasItem(item)).thenReturn(true);

    long itemPrice = vm.selectItemAndGetPrice(item);
    assertEquals(itemPrice, item.getPrice());
    verify(itemInventory, times(1)).hasItem(any());
  }

  @ParameterizedTest
  @EnumSource(Coin.class)
  void insertCoin_withValidCoins(Coin coin) {
    vm.setCurrentBalance(10);
    vm.insertCoin(coin);
    assertEquals(10 + coin.getDenomination(), vm.getCurrentBalance());
  }

  @ParameterizedTest
  @EnumSource(Item.class)
  void collectItemAndChange_withExactAmount(Item item) {
    vm.setCurrentItem(item);
    vm.setCurrentBalance(item.getPrice());
    cashInventory.put(Coin.QUARTER, 1);
    Bucket<Item, List<Coin>> collectItems = vm.collectItemAndChange();
    assertEquals(item, collectItems.getFirst());
    assertEquals(0, collectItems.getSecond().size());
  }

  @ParameterizedTest
  @EnumSource(Item.class)
  @DisplayName("more inserted money and return single change")
  void collectItemAndChange_withExtraAmount(Item item) {

    for (Coin coin : Coin.values()) {
      vm.setCurrentItem(item);
      vm.setCurrentBalance(item.getPrice() + coin.getDenomination());
      when(cashInventory.hasItem(any())).thenReturn(true);
      doNothing().when(itemInventory).deduct(any());

      Bucket<Item, List<Coin>> collectItems = vm.collectItemAndChange();
      assertEquals(item, collectItems.getFirst());
      assertEquals(1, collectItems.getSecond().size());
      assertEquals(coin, collectItems.getSecond().get(0));
    }
    verifyInventoryAndCashUpdates(4);
  }

  @ParameterizedTest
  @EnumSource(Item.class)
  @DisplayName("more inserted money and return single change of all coins")
  void collectItemAndChange_withExtraAmount2(Item item) {
    vm.setCurrentItem(item);
    vm.setCurrentBalance(item.getPrice() + 41);
    when(cashInventory.hasItem(any())).thenReturn(true);
    List<Coin> expectedCoins = List.of(Coin.values());

    Bucket<Item, List<Coin>> collectItems = vm.collectItemAndChange();
    assertEquals(item, collectItems.getFirst());
    assertEquals(4, collectItems.getSecond().size());

    assertThat("List equality without order",
        collectItems.getSecond(), containsInAnyOrder(expectedCoins.toArray()));
  }

  @ParameterizedTest
  @EnumSource(Item.class)
  @DisplayName("more inserted money and the extra coin is not available to give back")
  void collectItemAndChange_withExtraAmount21(Item item) {

    for (Coin coin : Coin.values()) {
      vm.setCurrentItem(item);
      vm.setCurrentBalance(item.getPrice() + coin.getDenomination());
      when(cashInventory.hasItem(coin)).thenReturn(false);

      NotSufficientChangeException exception = assertThrows(NotSufficientChangeException.class, () -> vm.collectItemAndChange());
      assertEquals("Not Sufficient change in Inventory", exception.getMessage());
      verifyInventoryAndCashUpdates(0);
    }
  }

  @ParameterizedTest
  @EnumSource(Item.class)
  @DisplayName("more inserted money and return single change of all coins")
  void collectItemAndChange_withRemainingAmount_throws_NotFullPaidException(Item item) {
    vm.setCurrentItem(item);
    vm.setCurrentBalance(item.getPrice() - 1);

    NotFullPaidException exception = assertThrows(NotFullPaidException.class, () -> vm.collectItemAndChange());
    assertEquals("Price not full paid, remaining : 1", exception.getMessage());
    verifyInventoryAndCashUpdates(0);
  }

  @Test
  void reset() {
    vm.reset();
    verify(itemInventory, times(1)).clear();
    verify(cashInventory, times(1)).clear();
    assertNull(vm.getCurrentItem());
    assertEquals(0, vm.getTotalSales());
    assertEquals(0, vm.getCurrentBalance());
  }


  @Test
  void refund_when_noCashAvailableInInventory() {
    Item item = Item.PEPSI;
    vm.setCurrentItem(item);
    vm.setCurrentBalance(item.getPrice());
    when(cashInventory.hasItem(any())).thenReturn(false);

    NotSufficientChangeException exception = assertThrows(NotSufficientChangeException.class, () -> vm.refund());
    assertEquals("Not Sufficient change in Inventory, Please buy another product", exception.getMessage());
  }

  @ParameterizedTest
  @EnumSource(Item.class)
  void refund_when_correctAmountOfChangeIsAvailable(Item item) {
    vm.setCurrentItem(item);
    vm.setCurrentBalance(item.getPrice());
    when(cashInventory.hasItem(any())).thenReturn(true);
    List<Coin> actualCoins = vm.refund();
    long total = actualCoins.stream()
        .mapToInt(Coin::getDenomination)
        .sum();

    assertEquals(total, item.getPrice());
  }

  private void verifyInventoryAndCashUpdates(int count) {
    verify(itemInventory, times(count)).deduct(any());
    verify(cashInventory, times(count)).deduct(any());
  }

}