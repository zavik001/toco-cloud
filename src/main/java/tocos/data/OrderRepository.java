package tocos.data;

import java.util.Optional;
import tocos.TocoOrder;

public interface OrderRepository {
    TocoOrder save(TocoOrder order);

    Optional<TocoOrder> findById(Long id);
}