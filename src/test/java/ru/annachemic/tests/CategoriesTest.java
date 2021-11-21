package ru.annachemic.tests;

import lombok.SneakyThrows;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import retrofit2.Response;
import ru.annachemic.db.model.Categories;
import ru.annachemic.dto.Category;
import ru.annachemic.service.CategoryService;
import ru.annachemic.utils.DbUtils;
import ru.annachemic.utils.RetrofitUtils;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CategoriesTest {
    static CategoryService categoryService;

    @BeforeAll
    static void beforeAll() {
        categoryService = RetrofitUtils.getRetrofit().create(CategoryService.class);
    }
    @SneakyThrows
    @Test
    void getCategoryByIdPositiveTest() {
        Long categoryID=DbUtils.getCategoryID("Food");
        Response<Category> response = categoryService.getCategory(Math.toIntExact(categoryID)).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
    }

    @SneakyThrows
    @Test
    void getCategoryWithResponseAssertionsPositiveTest() {
        Long categoryID=DbUtils.getCategoryID("Food");
        Response<Category> response = categoryService.getCategory(Math.toIntExact(categoryID)).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        assertThat(response.body().getId(), equalTo(1));
        assertThat(response.body().getTitle(), equalTo("Food"));
        response.body().getProducts().forEach(product ->
                assertThat(product.getCategoryTitle(), equalTo("Food")));
    }

    @SneakyThrows
    @Test
    void testThatAllCategoriesPricesMoreThanZero() {
        List<Categories> categories= DbUtils.getCategories();
        for (Categories c:categories
             ) {
            Response<Category> response = categoryService.getCategory(c.getId()).execute();
            response.body().getProducts().forEach(product ->
                    assertThat(product.getPrice(), greaterThan(0)));
        }
    }
}
