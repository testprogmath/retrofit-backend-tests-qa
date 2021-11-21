package ru.annachemic.tests;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import ru.annachemic.db.dao.ProductsMapper;
import ru.annachemic.db.model.Products;
import ru.annachemic.dto.Product;
import ru.annachemic.enums.CategoryType;
import ru.annachemic.dto.Category;
import ru.annachemic.service.CategoryService;
import ru.annachemic.service.ProductService;
import ru.annachemic.utils.DbUtils;
import ru.annachemic.utils.RetrofitUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ProductTests {
    int productId;
    static ProductsMapper productsMapper;
    static Retrofit client;
    static ProductService productService;
    static CategoryService categoryService;
    Faker faker = new Faker();
    Product product;
    boolean skipClean = false;

    @BeforeAll
    static void beforeAll() {
        client = RetrofitUtils.getRetrofit();
        productService = client.create(ProductService.class);
        categoryService = client.create(CategoryService.class);
        productsMapper = DbUtils.getProductsMapper();
    }

    @BeforeEach
    void setUp() {
        product = new Product()
                .withTitle(faker.food().dish())
                .withPrice((int) ((Math.random() + 1) * 100))
                .withCategoryTitle(CategoryType.FOOD.getTitle());
    }

    @Test
    void postProductTest() throws IOException {
        Integer countProductsBefore = DbUtils.countProducts();
        Response<Product> response = productService.createProduct(product).execute();
        Integer countProductsAfter = DbUtils.countProducts();
        assertThat(response.body().getTitle(), equalTo(product.getTitle()));
        assertThat(response.body().getPrice(), equalTo(product.getPrice()));
        assertThat(countProductsAfter,greaterThan(countProductsBefore));
        assertThat(response.body().getCategoryTitle(), equalTo(product.getCategoryTitle()));
        productId = response.body().getId();
    }

    @Test
    @SneakyThrows
    void checkThatAllProductsAreUniqueTest() {
        List<Products> productsList = DbUtils.getAllProducts();
        HashMap<String,Integer> hashMap = new HashMap<>();
        for (Products p:productsList
             ) {
            assertThat(hashMap.containsKey(p.getTitle()),equalTo(false));
            hashMap.put(p.getTitle(),1);
        }
    }

    @Test
    void updateTest() throws IOException {
        skipClean=true;
        Response<Product> createProductResponse = productService.createProduct(product).execute();
        assertThat(createProductResponse.body().getTitle(), equalTo(product.getTitle()));
        productId = createProductResponse.body().getId();
        Products p = new Products();
        p.setId((long) productId);
        p.setTitle("NewTitle");
        p.setPrice(createProductResponse.body().getPrice());
        Long categoryID=DbUtils.getCategoryID(product.getCategoryTitle());
        p.setCategory_id(categoryID);
        int updatedCount = DbUtils.updateProduct(p);
        assertThat(updatedCount,greaterThan(0));
        System.out.println(p.getId());
        Response<Product> response = productService.getProduct(productId).execute();
        assertThat(response.body().getTitle(), equalTo("NewTitle"));
    }

    @Test
    void getCategoryByIdTest() throws IOException {
        skipClean=true;
        Integer id = CategoryType.FOOD.getId();
        Response<Category> response = categoryService
                .getCategory(id)
                .execute();
        assertThat(response.body().getTitle(), equalTo(CategoryType.FOOD.getTitle()));
        assertThat(response.body().getId(), equalTo(id));
    }

    @Test
    @SneakyThrows
    void deleteDeletedProductTest() {
        skipClean=true;
        Response<Product> response = productService.createProduct(product).execute();
        int id = response.body().getId();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        int countDeleted=DbUtils.deleteProduct((long) id);
        assertThat(countDeleted, greaterThan(0));
        Response<ResponseBody> response2 = productService.deleteProduct(id).execute();
        assertThat(response2.isSuccessful(), CoreMatchers.is(false));
    }

    @Test
    @SneakyThrows
    void updateNotExistTest(){
        postProductTest();
        tearDown();
        skipClean=true;
        product=product.withId(productId);
        product=product.withTitle("NewSuperName");
        Response<Product> response = productService.updateProduct(product).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(false));
    }

    @Test
    @SneakyThrows
    void getCreatedTest(){
        postProductTest();
        Products p=DbUtils.getProducts((long) productId);
        assertThat(p.getTitle(),CoreMatchers.is(product.getTitle()));
    }

    @Test
    @SneakyThrows
    void getNotExistTest(){
        postProductTest();
        tearDown();
        skipClean=true;
        Response<Product> response = productService.getProduct(productId).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(false));
    }
    @Test
    @SneakyThrows
    void checkNonNegativePriceTest() {
        product = product.withPrice((int) (Math.random() * -10000));
        Response<Product> response = productService.createProduct(product).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(false));
    }

    @Test
    @SneakyThrows
    void checkCreationProductWithNotExistCategoryTest() {
        skipClean=true;
        product = product.withCategoryTitle("NotExistCategory");
        Response<Product> response = productService.createProduct(product).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(false));
    }

    @Test
    @SneakyThrows
    @DisplayName("create product with ID")
    void createProductWithIDTest() {
        skipClean=true;
        product=product.withId(10);
        Response<Product> response = productService.createProduct(product).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(false));
    }
    @AfterEach
    void tearDown() throws IOException {
        if (skipClean){
            return;
        }
        int res = DbUtils.deleteProduct((long) productId);
        assertThat(res, greaterThan(0));
    }
}
