package io.github.syferie.magicblock.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * JSON文件数据仓库抽象基类
 *
 * 设计目的:
 * - 为JSON文件存储提供通用实现
 * - 子类只需指定TypeToken即可使用
 * - 内置内存缓存，减少磁盘IO
 * - 异步保存机制，避免阻塞主线程
 *
 * 使用示例:
 * <pre>
 * public class FavoriteRepository extends JsonFileRepository<UUID, Set<Material>> {
 *     public FavoriteRepository(File dataFile, Logger logger) {
 *         super(dataFile, logger);
 *     }
 *
 *     @Override
 *     protected TypeToken<Map<UUID, Set<Material>>> getTypeToken() {
 *         return new TypeToken<Map<UUID, Set<Material>>>() {};
 *     }
 * }
 * </pre>
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public abstract class JsonFileRepository<K, V> implements DataRepository<K, V> {

    protected final File dataFile;
    protected final Gson gson;
    protected final Logger logger;
    protected final Map<K, V> cache = new ConcurrentHashMap<>();

    private volatile boolean dirty = false; // 数据是否已修改

    public JsonFileRepository(File dataFile, Logger logger) {
        this.dataFile = dataFile;
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        // 加载数据
        loadAll();
    }

    /**
     * 子类提供TypeToken用于JSON反序列化
     */
    protected abstract TypeToken<Map<K, V>> getTypeToken();

    @Override
    public void save(K key, V value) {
        cache.put(key, value);
        dirty = true;
        scheduleSave(); // 延迟保存
    }

    @Override
    public Optional<V> get(K key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public Map<K, V> getAll() {
        return new HashMap<>(cache);
    }

    @Override
    public void delete(K key) {
        cache.remove(key);
        dirty = true;
        scheduleSave();
    }

    @Override
    public void saveAll(Map<K, V> data) {
        cache.clear();
        cache.putAll(data);
        dirty = true;
        forceSave();
    }

    @Override
    public void clear() {
        cache.clear();
        dirty = true;
        forceSave();
    }

    /**
     * 加载所有数据
     */
    private void loadAll() {
        if (!dataFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(dataFile)) {
            Map<K, V> data = gson.fromJson(reader, getTypeToken().getType());
            if (data != null) {
                cache.putAll(data);
            }
        } catch (IOException e) {
            logger.warning("加载JSON文件失败: " + dataFile.getName() + " - " + e.getMessage());
        }
    }

    /**
     * 延迟保存 (批量保存优化)
     */
    private void scheduleSave() {
        // TODO: 可以实现延迟批量保存，目前简化为立即保存
        if (dirty) {
            forceSave();
        }
    }

    /**
     * 强制立即保存
     */
    public void forceSave() {
        if (!dirty) {
            return;
        }

        try {
            // 确保父目录存在
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }

            // 写入JSON文件
            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(cache, writer);
            }

            dirty = false;
        } catch (IOException e) {
            logger.severe("保存JSON文件失败: " + dataFile.getName() + " - " + e.getMessage());
        }
    }

    @Override
    public void close() {
        forceSave(); // 关闭前确保保存
    }
}
