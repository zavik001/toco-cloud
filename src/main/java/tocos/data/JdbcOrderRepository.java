package tocos.data;

import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.asm.Type;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tocos.IngredientRef;
import tocos.Toco;
import tocos.TocoOrder;

@Repository
public class JdbcOrderRepository implements OrderRepository {

    private JdbcOperations jdbcOperations;

    public JdbcOrderRepository(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    @Transactional
    public TocoOrder save(TocoOrder order) {
        PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(
                "insert into Taco_order "
                        + "(delivery_name, delivery_street, delivery_city, "
                        + "delivery_state, delivery_zip, cc_number, "
                        + "cc_expiration, cc_cvv, placed_at) "
                        + "values (?,?,?,?,?,?,?,?,?)",
                Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP);
        pscf.setReturnGeneratedKeys(true);
        order.setPlacedAt(new Date());
        PreparedStatementCreator psc = pscf.newPreparedStatementCreator(Arrays.asList(
                order.getDeliveryName(),
                order.getDeliveryStreet(),
                order.getDeliveryCity(),
                order.getDeliveryState(),
                order.getDeliveryZip(),
                order.getCcNumber(),
                order.getCcExpiration(),
                order.getCcCVV(),
                order.getPlacedAt()));

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcOperations.update(psc, keyHolder);
        long orderId = keyHolder.getKey().longValue();
        order.setId(orderId);

        List<Toco> tocos = order.getTacos();
        int i = 0;
        for (Toco toco : tocos) {
            saveTaco(orderId, i++, toco);
        }
        return order;
    }

    private long saveTaco(long orderId, int orderKey, Toco toco) {

        toco.setDate(new Date());

        PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(
                "insert into Toco "
                        + "(name, created_at, taco_order, taco_order_key) "
                        + "values (?, ?, ?, ?)",
                Types.VARCHAR, Types.TIMESTAMP, Type.LONG, Type.LONG);

        pscf.setReturnGeneratedKeys(true);
        PreparedStatementCreator psc = pscf.newPreparedStatementCreator(
                Arrays.asList(
                        toco.getName(),
                        toco.getDate(),
                        orderId,
                        orderKey));

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcOperations.update(psc, keyHolder);
        long tocoId = keyHolder.getKey().longValue();
        toco.setId(tocoId);

        saveIngredientRefs(tocoId, toco.getIngredients());

        return tocoId;
    }

    private void saveIngredientRefs(long id, List<IngredientRef> ingredientRefs) {
        int key = 0;
        for (IngredientRef ingredient : ingredientRefs) {
            jdbcOperations.update(
                    "insert into Ingredient_Ref (ingredient, toco, taco_key) "
                            + "values (?, ?, ?)",
                    ingredient.getIngredient(), id, key++);
        }
    }

    @Override
    public Optional<TocoOrder> findById(Long id) {
        try {
            TocoOrder order = jdbcOperations.queryForObject(
                    "select id, delivery_name, delivery_street, delivery_city, "
                            + "delivery_state, delivery_zip, cc_number, cc_expiration, "
                            + "cc_cvv, placed_at from Taco_Order where id=?",
                    (row, rowNum) -> {
                        TocoOrder tocoOrder = new TocoOrder();
                        tocoOrder.setId(row.getLong("id"));
                        tocoOrder.setDeliveryName(row.getString("delivery_name"));
                        tocoOrder.setDeliveryStreet(row.getString("delivery_street"));
                        tocoOrder.setDeliveryCity(row.getString("delivery_city"));
                        tocoOrder.setDeliveryState(row.getString("delivery_state"));
                        tocoOrder.setDeliveryZip(row.getString("delivery_zip"));
                        tocoOrder.setCcNumber(row.getString("cc_number"));
                        tocoOrder.setCcExpiration(row.getString("cc_expiration"));
                        tocoOrder.setCcCVV(row.getString("cc_cvv"));
                        tocoOrder.setPlacedAt(new Date(row.getTimestamp("placed_at").getTime()));
                        tocoOrder.setTacos(findTacosByOrderId(row.getLong("id")));
                        return tocoOrder;
                    }, id);
            return Optional.of(order);
        } catch (IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }
    }

    private List<Toco> findTacosByOrderId(long orderId) {
        return jdbcOperations.query(
                "select id, name, created_at from Toco "
                        + "where taco_order=? order by taco_order_key",
                (row, rowNum) -> {
                    Toco toco = new Toco();
                    toco.setId(row.getLong("id"));
                    toco.setName(row.getString("name"));
                    toco.setDate(new Date(row.getTimestamp("created_at").getTime()));
                    toco.setIngredients(findIngredientsByTacoId(row.getLong("id")));
                    return toco;
                },
                orderId);
    }

    private List<IngredientRef> findIngredientsByTacoId(long tocoId) {
        return jdbcOperations.query(
                "select ingredient from Ingredient_Ref "
                        + "where toco = ? order by taco_key",
                (row, rowNum) -> {
                    return new IngredientRef(row.getString("ingredient"));
                },
                tocoId);
    }
}