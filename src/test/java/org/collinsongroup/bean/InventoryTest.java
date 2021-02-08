package org.collinsongroup.bean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;

import static org.junit.Assert.*;

class InventoryTest {

  Inventory<Item> inventory;

  @BeforeEach
  void init() {
    inventory = new Inventory<>();
  }

  @ParameterizedTest
  @EnumSource(Item.class)
  void add_itemsQuantity(Item item) {
    inventory.put(item, 1);
    inventory.add(item);
    assertEquals(2, inventory.getQuantity(item));
  }

  @ParameterizedTest
  @EnumSource(Item.class)
  void deduct_withAllItems(Item item) {
    inventory.put(item, 1);
    inventory.add(item);
    inventory.deduct(item);
    assertEquals(1, inventory.getQuantity(item));
  }

  @Test
  void clear_withAllItems() {
    Arrays.stream(Item.values()).forEach(item-> inventory.put(item, 1));
    inventory.clear();
    Arrays.stream(Item.values()).forEach(item-> assertFalse(inventory.hasItem(item)));
  }


  @ParameterizedTest
  @EnumSource(Item.class)
  void testPutAnGetQuantity(Item item) {
    inventory.put(item, 5);
    assertEquals(5, inventory.getQuantity(item));
  }
}