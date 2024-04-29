package ru.hse.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.hse.bot.BotLogic;
import ru.hse.IStateStorage;
import ru.hse.SimpleMemoryStorage;

import static org.junit.jupiter.api.Assertions.*;

public class BotLogicTests {
    private SimpleMemoryStorage storage;
    private BotLogic bl ;
    private static final long userId = 1l;
    @BeforeEach
    public void beforeEach(){
        storage = new SimpleMemoryStorage();
        bl = new BotLogic();
    }
    @ParameterizedTest
    @ValueSource(strings = {"buy", "orders", "deposit", "account"})
    public void testMainMenuPoints(String point){
        String res = bl.reactOnMessage("", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains(point));
    }
    @Test
    public void testWrongCommand(){
        bl.reactOnMessage("wrong", userId, storage);
        String res = bl.reactOnMessage("string", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("команда не распознана"));
    }

    @Test
    public void testWrongStorage(){
        String res = bl.reactOnMessage("wrong", 1, null);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("storage не может быть null"));
    }
    @Test
    public void testWrongUserId(){
        String res = bl.reactOnMessage("wrong", 0, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("userid не может быть меньше"));
        bl.reactOnMessage("wrong", 100001, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("userid не может быть меньше"));
    }
    @ParameterizedTest
    @ValueSource(strings = {"good with high price(100 ", "good2(200 ", ": 0"})
    public void testBuyPoints(String point){
        String res = bl.reactOnMessage("buy", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains(point));
    }
    @Test
    public void testBuyNonString(){
        bl.reactOnMessage("buy", userId, storage);
        String res = bl.reactOnMessage("string", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("не является числом"));
    }

    @Test
    public void testBuyWrongGood(){
        bl.reactOnMessage("buy", userId, storage);
        String res = bl.reactOnMessage("4", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("выбранный товар не существует"));
    }
    @Test
    public void testBuyAndBack(){
        bl.reactOnMessage("buy", userId, storage);
        String res = bl.reactOnMessage("0", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("добро пожа"));
    }
    @Test
    public void testBuyAndCheckout(){
        bl.reactOnMessage("buy", userId, storage);
        String res = bl.reactOnMessage("checkout", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("добро пожа"));
    }
    @Test
    public void testBuyAndCheckoutNoSuch(){
        String res;
        bl = createBotWithBalance(1, storage);
        BotLogic b2 = createBotWithBalance(2, storage);

        bl.reactOnMessage("buy", userId, storage);
        bl.reactOnMessage("1", userId, storage);
        bl.reactOnMessage("60", userId, storage);

        b2.reactOnMessage("buy", 2, storage);
        b2.reactOnMessage("1", 2, storage);
        b2.reactOnMessage("50", 2, storage);
        b2.reactOnMessage("checkout", 2, storage);

        res = bl.reactOnMessage("checkout", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("выбранного товара нет в наличии в магазине"));
    }

    @Test
    public void testBuyNoGoods(){
        BotLogic bl2 = createBotWithOrder(2, storage, 100, 200, 0);
        String res;
        bl = createBotWithBalance(1, storage);
        bl.reactOnMessage("buy", userId, storage);
        bl.reactOnMessage("1", userId, storage);
        res = bl.reactOnMessage("1", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("товара нет в наличии в магазине"));
    }
    @Test
    public void testBucketIncorrect(){
        String res;
        bl = createBotWithBalance(1, storage);
        bl.reactOnMessage("buy", userId, storage);
        bl.reactOnMessage("1", userId, storage);
        res = bl.reactOnMessage("101", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("нет в наличии в магазине"));

        bl.reactOnMessage("1", userId, storage);
        res = bl.reactOnMessage("0", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("напишите номер интересующего"));

        bl.reactOnMessage("buy", userId, storage);
        bl.reactOnMessage("1", userId, storage);
        res = bl.reactOnMessage("ff", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("не является числом"));

        bl.reactOnMessage("buy", userId, storage);
        bl.reactOnMessage("1", userId, storage);
        res = bl.reactOnMessage("-10", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("положительным"));
    }
    @Test
    public void testBucketAddRemove(){
        String res;
        bl = createBotWithBalance(1, storage);
        bl.reactOnMessage("buy", userId, storage);
        bl.reactOnMessage("1", userId, storage);
        bl.reactOnMessage("10", userId, storage);

        bl.reactOnMessage("-1", userId, storage);
        res = bl.reactOnMessage("10", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains(" товары удалены"));

    }
    @Test
    public void testBucketRemoveIncorrect(){
        String res;
        bl = createBotWithBalance(1, storage);
        bl.reactOnMessage("buy", userId, storage);
        res = bl.reactOnMessage("-1", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains(" не найден в корзине"));

        res = bl.reactOnMessage("-2", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains(" не найден в корзине"));

    }
    @Test
    public void testCheckoutSuccess(){
        String res;
        bl = createBotWithBalance(1, storage);

        bl.reactOnMessage("buy", userId, storage);
        bl.reactOnMessage("1", userId, storage);
        bl.reactOnMessage("60", userId, storage);

        res = bl.reactOnMessage("checkout", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("успешно добавлен, номер заказа:"));
    }
    @Test
    public void testCheckoutWithOrderCheck(){
        String res;
        bl = createBotWithBalance(1, storage);

        bl.reactOnMessage("buy", userId, storage);
        bl.reactOnMessage("1", userId, storage);
        bl.reactOnMessage("10", userId, storage);

        res = bl.reactOnMessage("checkout", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        int ord = res.indexOf("номер заказа:");
        String orderId = res.substring(ord+14);
        orderId = orderId.substring(0, orderId.indexOf("\n"));

        res = bl.reactOnMessage("orders", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();

        assertTrue(res.contains("имеющиесся заказы"));
        assertTrue(res.contains("1. good with high price(10)"));

        bl.reactOnMessage("0", userId, storage);
    }

    @Test
    public void testBuyNoMoney(){
        String res;
        bl.reactOnMessage("buy", userId, storage);
        bl.reactOnMessage("1", userId, storage);
        res = bl.reactOnMessage("50",userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("средств недостаточно"));
    }

    @Test
    public void testBucketRemoveCheckout(){
        String res;
        bl = createBotWithBalance(userId, storage);

        bl.reactOnMessage("buy", userId, storage);
        bl.reactOnMessage("1", userId, storage);
        bl.reactOnMessage("10", userId, storage);
        bl.reactOnMessage("1", userId, storage);
        bl.reactOnMessage("10", userId, storage);

        bl.reactOnMessage("-1", userId, storage);
        res = bl.reactOnMessage("30", userId, storage);

        bl.reactOnMessage("-1", userId, storage);
        res = bl.reactOnMessage("10", userId, storage);

        bl.reactOnMessage("checkout", userId, storage);

        res = bl.reactOnMessage("buy", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("счету 2850"));
        assertTrue(res.contains("1. good with high price(90"));

        bl.reactOnMessage("0", userId, storage);
    }
    @Test
    public void testOrderFullInfo(){
        bl = createBotWithOrder(userId, storage, 10, 0, 0);
        String res;
        bl.reactOnMessage("orders", userId, storage);
        res = bl.reactOnMessage("1", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();

        assertTrue(res.contains("good with high price(10)"));
        assertTrue(res.contains("описания нет"));
    }
    @Test
    public void testOrderInfoShort(){
        bl = createBotWithOrder(userId, storage, 10, 0, 11);
        String res;
        res = bl.reactOnMessage("orders", userId, storage);
        //res = bl.reactOnMessage("1", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();

        assertTrue(res.contains("good with high price(10)"));
        assertTrue(res.contains("(11)"));
    }

    @Test
    public void testOrderInfoLong(){
        bl = createBotWithOrder(userId, storage, 10, 1, 11);
        String res;
        bl.reactOnMessage("orders", userId, storage);
        res = bl.reactOnMessage("1", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();

        assertTrue(res.contains("good with high price(10)"));
        assertTrue(res.contains("good2(1)"));
        assertTrue(res.contains("mean(11)"));
    }
    @Test
    public void testOrdersNoOrders(){
        String res;
        res = bl.reactOnMessage("orders", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("заказов нет"));
    }

    @Test
    public void testOrdersIncorrect(){
        String res;
        bl.reactOnMessage("orders", userId, storage);

        res = bl.reactOnMessage("string", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("не является числом"));

        res = bl.reactOnMessage("1", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("заказ не существует"));

    }
    @Test
    public void accountInitialTest(){
        String res;
        res = bl.reactOnMessage("account", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("у вас на счету "));
    }
    @Test
    public void accountTestMoney(){
        String res;
        bl = createBotWithBalance(userId, storage);
        res = bl.reactOnMessage("account", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("у вас на счету 3000"));
    }

    @Test
    public void testDepositIncorrect(){
        String res;
        bl.reactOnMessage("deposit", userId, storage);
        res = bl.reactOnMessage("1", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("код 1 не валиден"));
        res = bl.reactOnMessage("3DRwBBrcFThKXq9zNIdPihfg3eaQ7g", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("средства внесены, на счету"));

        bl.reactOnMessage("3DRwBBrcFThKXq9zNIdPihfg3eaQ7g", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("средства внесены, на счету"));

        bl.reactOnMessage("0", userId, storage);

        res = bl.reactOnMessage("account", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("на счету 1000"));
    }
    @Test
    public void testEmtpyStorage(){
        String res;
        IStateStorage storage = new SimpleMemoryStorage(true);
        bl = createBotWithBalance(1, storage);

        res = bl.reactOnMessage("buy", userId, storage);
        assertNotNull(res);
        res = res.toLowerCase();
        assertTrue(res.contains("товаров нет."));
    }

    private BotLogic createBotWithBalance(long id, IStateStorage storage) {
        BotLogic bl = new BotLogic();
        bl.reactOnMessage("deposit", id, storage);
        bl.reactOnMessage("lyvQuGNBYhrcnDfCsn9cchqrTvLEtg",id, storage);
        bl.reactOnMessage("0awdnDr9kaByy4qg4ha94A2HXcRCJz",id, storage);
        bl.reactOnMessage("3DRwBBrcFThKXq9zNIdPihfg3eaQ7g",id, storage);
        bl.reactOnMessage("0", id, storage);
        return bl;
    }
    private BotLogic createBotWithOrder(long id, IStateStorage storage, int cnt, int cnt2, int cnt3) {
        BotLogic bl = createBotWithBalance(id, storage);

        if(cnt>0) {
            bl.reactOnMessage("buy", id, storage);
            bl.reactOnMessage("1", id, storage);
            bl.reactOnMessage(Integer.toString(cnt), id, storage);
        }
        if(cnt2>0) {
            bl.reactOnMessage("buy", id, storage);
            bl.reactOnMessage("2", id, storage);
            bl.reactOnMessage(Integer.toString(cnt2), id, storage);
        }
        if(cnt3>0) {
            bl.reactOnMessage("buy", id, storage);
            bl.reactOnMessage("3", id, storage);
            bl.reactOnMessage(Integer.toString(cnt3), id, storage);
        }
        String ret = bl.reactOnMessage("checkout", id, storage);
        return bl;
    }
}
