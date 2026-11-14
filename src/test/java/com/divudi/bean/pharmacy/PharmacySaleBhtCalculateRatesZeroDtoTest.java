package com.divudi.bean.pharmacy;

import com.divudi.core.data.dto.StockDTO;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.pharmacy.ItemBatch;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.core.entity.pharmacy.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for issue #25605:
 * Inward ward-request issue line can be saved with rate 0 although stock is priced.
 * Ensures calculateRates prefers the stock item batch retail rate when DTO rate is 0 or missing.
 */
public class PharmacySaleBhtCalculateRatesZeroDtoTest {

    private PharmacySaleBhtController controller;
    private BillItem billItem;
    private PharmaceuticalBillItem pharmaceuticalBillItem;
    private Stock stock;
    private ItemBatch itemBatch;
    private Item item;

    @BeforeEach
    public void setUp() {
        controller = new PharmacySaleBhtController();

        item = new Item();
        item.setName("Paracetamol 500mg");

        itemBatch = new ItemBatch();
        itemBatch.setItem(item);
        itemBatch.setRetailsaleRate(150.0);

        stock = new Stock();
        stock.setId(101L);
        stock.setItemBatch(itemBatch);

        pharmaceuticalBillItem = new PharmaceuticalBillItem();
        pharmaceuticalBillItem.setStock(stock);

        billItem = new BillItem();
        billItem.setItem(item);
        billItem.setQty(2.0);
        billItem.setPharmaceuticalBillItem(pharmaceuticalBillItem);
    }

    @Test
    public void testCalculateRatesPrefersBatchRateWhenSelectedStockDtoRateIsZero() {
        StockDTO dto = new StockDTO();
        dto.setId(101L);
        dto.setRetailRate(0.0);
        controller.setSelectedStockDto(dto);

        controller.calculateRates(billItem);

        assertEquals(150.0, billItem.getRate(), 0.001);
        assertEquals(300.0, billItem.getGrossValue(), 0.001);
        assertEquals(300.0, billItem.getNetValue(), 0.001);
    }

    @Test
    public void testCalculateRatesUsesDtoRateWhenNonZero() {
        StockDTO dto = new StockDTO();
        dto.setId(101L);
        dto.setRetailRate(120.0);
        controller.setSelectedStockDto(dto);

        controller.calculateRates(billItem);

        assertEquals(120.0, billItem.getRate(), 0.001);
        assertEquals(240.0, billItem.getGrossValue(), 0.001);
        assertEquals(240.0, billItem.getNetValue(), 0.001);
    }

    @Test
    public void testCalculateRatesFallsBackToBatchRateWhenNoDto() {
        controller.setSelectedStockDto(null);

        controller.calculateRates(billItem);

        assertEquals(150.0, billItem.getRate(), 0.001);
        assertEquals(300.0, billItem.getGrossValue(), 0.001);
        assertEquals(300.0, billItem.getNetValue(), 0.001);
    }
}
