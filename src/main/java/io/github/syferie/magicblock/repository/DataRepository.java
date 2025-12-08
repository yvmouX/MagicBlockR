package io.github.syferie.magicblock.repository;

import java.util.Map;
import java.util.Optional;

/**
 * 数据仓库接口 - Repository模式
 *
 * 设计目的:
 * - 抽象数据访问层，隔离业务逻辑和存储实现
 * - 支持多种存储后端 (JSON, YAML, MySQL, Redis等)
 * - 统一数据访问接口
 *
 * 泛型参数:
 * - K: 键类型 (如 UUID)
 * - V: 值类型 (如 BlockBindData, Set<Material>)
 *
 * 实现示例:
 * - JsonFileRepository - JSON文件存储
 * - DatabaseRepository - 数据库存储
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public interface DataRepository<K, V> {

    /**
     * 保存数据
     *
     * @param key 键
     * @param value 值
     */
    void save(K key, V value);

    /**
     * 获取数据
     *
     * @param key 键
     * @return Optional包装的值
     */
    Optional<V> get(K key);

    /**
     * 获取所有数据
     *
     * @return 所有数据的Map
     */
    Map<K, V> getAll();

    /**
     * 删除数据
     *
     * @param key 键
     */
    void delete(K key);

    /**
     * 批量保存
     *
     * @param data 要保存的数据Map
     */
    void saveAll(Map<K, V> data);

    /**
     * 检查是否存在
     *
     * @param key 键
     * @return 如果存在返回true
     */
    default boolean exists(K key) {
        return get(key).isPresent();
    }

    /**
     * 清空所有数据
     */
    void clear();

    /**
     * 关闭资源
     */
    default void close() {
        // 默认实现为空，需要清理资源的实现可以覆盖
    }
}
