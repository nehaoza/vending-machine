package org.collinsongroup.service;


import org.collinsongroup.bean.Bucket;
import org.collinsongroup.bean.Coin;
import org.collinsongroup.bean.Inventory;
import org.collinsongroup.bean.Item;
import org.collinsongroup.exception.SoldOutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VendingMachineTest {

  @Mock
  VendingMachine vm;

  @Mock
  Inventory<Coin> cashInventory;

  @Mock
  Inventory<Item> itemInventory;

  @BeforeEach
  public void init() {
    vm = new VendingMachine(cashInventory, itemInventory);
  }

  @Test
  @DisplayName(value = "hello")
  public void selectItemAndGetPrice_withInvalidItem() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      vm.selectItemAndGetPrice(null);
    });
    assertEquals("Please provide a valid Item to buy", exception.getMessage());
  }

  @Test
  public void selectItemAndGetPrice_whenItemIsNotAvailable() {
    when(itemInventory.hasItem(eq(Item.COKE))).thenReturn(false);

    SoldOutException exception = assertThrows(SoldOutException.class, () -> {
      vm.selectItemAndGetPrice(Item.COKE);
    });
    assertEquals("Sold Out, Please buy another item", exception.getMessage());

    verify(itemInventory, times(1)).hasItem(any());
  }

  @ParameterizedTest
  @EnumSource(Item.class)
  public void selectItemAndGetPrice_withValidItem(Item item) {
    when(itemInventory.hasItem(eq(item))).thenReturn(true);

    long itemPrice = vm.selectItemAndGetPrice(item);
    assertEquals(itemPrice, item.getPrice());
    verify(itemInventory, times(1)).hasItem(any());
  }

  @ParameterizedTest
  @EnumSource(Coin.class)
  public void insertCoin_withValidCoins(Coin coin) {
    vm.setCurrentBalance(10);
    vm.insertCoin(coin);
    assertEquals(10 + coin.getDenomination(), vm.getCurrentBalance());
  }

  @ParameterizedTest
  @EnumSource(Item.class)
  public void collectItemAndChange_withExactAmount(Item item) {
    vm.setCurrentItem(item);
    vm.setCurrentBalance(item.getPrice());
    cashInventory.put(Coin.QUARTER, 1);
    Bucket<Item, List<Coin>> collectItems = vm.collectItemAndChange();
    assertEquals(item, collectItems.getFirst());
    assertEquals(0, collectItems.getSecond().size());
  }

  @ParameterizedTest
  @EnumSource(Item.class)
  @DisplayName("more inserted money and no change to give")
  public void collectItemAndChange_withExtraNickleAmount(Item item) {

    for(Coin coin: Coin.values()) {
      vm.setCurrentItem(item);
      vm.setCurrentBalance(item.getPrice() + coin.getDenomination());
      when(cashInventory.hasItem(any())).thenReturn(true);

      Bucket<Item, List<Coin>> collectItems = vm.collectItemAndChange();
      assertEquals(item, collectItems.getFirst());
      assertEquals(1, collectItems.getSecond().size());
      assertEquals(coin, collectItems.getSecond().get(0));
    }
  }

}