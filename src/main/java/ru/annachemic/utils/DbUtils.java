package ru.annachemic.utils;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import ru.annachemic.db.dao.CategoriesMapper;
import ru.annachemic.db.dao.ProductsMapper;
import ru.annachemic.db.model.Categories;
import ru.annachemic.db.model.CategoriesExample;
import ru.annachemic.db.model.Products;
import ru.annachemic.db.model.ProductsExample;

import java.io.IOException;
import java.util.List;

@UtilityClass
public class DbUtils {
    static Faker faker = new Faker();
    private static final String resource = "mybatisConfig.xml";

    private static SqlSession getSqlSession() throws IOException {
        SqlSessionFactory sqlSessionFactory;
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsStream(resource));
        return sqlSessionFactory.openSession(true);
    }

    @SneakyThrows
    public static CategoriesMapper getCategoriesMapper() {
        return getSqlSession().getMapper(CategoriesMapper.class);
    }

    @SneakyThrows
    public static ProductsMapper getProductsMapper() {
        return getSqlSession().getMapper(ProductsMapper.class);
    }

    public Integer countProducts() {
        long products = getProductsMapper().countByExample(new ProductsExample());
        return Math.toIntExact(products);
    }

    public List<Products> getAllProducts() {
        return getProductsMapper().selectByExample(new ProductsExample());
    }

    public Products getProducts(Long id) {
        return getProductsMapper().selectByPrimaryKey(id);
    }

    public List<Categories> getCategories() {
        return getCategoriesMapper().selectByExample(new CategoriesExample());
    }

    public Long getCategoryID(String title) {
        List<Categories> categories = getCategoriesMapper().selectByExample(new CategoriesExample());
        int cId = 0;
        for (Categories c : categories
        ) {
            System.out.println(c.getTitle() + c.getId() + title);
            if (c.getTitle().equals(title)) {
                cId = c.getId();
                break;
            }
        }
        return new Long(cId);
    }

    public int deleteProduct(Long id) {
        return getProductsMapper().deleteByPrimaryKey(id);
    }

    public int updateProduct(Products product) {
        return getProductsMapper().updateByPrimaryKey(product);
    }
}
