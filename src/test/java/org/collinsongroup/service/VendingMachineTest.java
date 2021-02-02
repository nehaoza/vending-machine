package org.collinsongroup.service;


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

    long itemPrice = vm.selectItemAndGetPrice(Item.COKE);
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


}