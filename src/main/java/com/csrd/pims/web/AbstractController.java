package com.csrd.pims.web;

import com.baomidou.mybatisplus.extension.service.IService;
import com.csrd.pims.bean.web.ResultWrapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基类，提供了基本的增删查改 WEBAPI 接口
 * 继承此类即可提供基本的增删查改接口
 * @param <S> 要使用的接口类
 * @param <T> 数据库映射实体类
 * @author lichengkun
 */
public abstract class AbstractController<S extends IService<T>, T> {
    /**
     * 业务层，因为是继承的IService接口，所以如果要自定义业务的话可以重写其中的方法.....
     */
    @Autowired
    @SuppressWarnings("all")
    protected S service;

    /**
     * 基础QUERY方法
     * @return {@link ResultWrapper}
     */
    @GetMapping
    @ApiOperation(value = "通用查询接口，查询所有资源.", notes = "获取指定路径所有资源，内部查询使用的是mybatis-plus的list接口")
    public ResultWrapper<List<T>> queryAll() {
        return ResultWrapper.success(service.list());
    }

    /**
     * 根据ID查询信息
     * @param id ID
     * @return {@link ResultWrapper}
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "通用查询接口，指定id查询", notes = "根据id获取指定Rest资源信息，内部查询使用的是mybatis-plus的getById接口")
    public ResultWrapper<T> queryInfoById(@PathVariable @ApiParam(value = "需要查询的资源id") String id) {
        return ResultWrapper.success(service.getById(id));
    }

    /**
     * 基础ADD方法
     * @param entity 添加的实体类
     * @return {@link ResultWrapper}
     */
    @PostMapping
    @ApiOperation(value = "通用add接口", notes = "restful风格的add接口，内部插值使用的是mybatis-plus的save接口")
    public ResultWrapper<Boolean> save(@RequestBody @ApiParam(value = "需要添加的实体类") T entity) {
        return ResultWrapper.success(service.save(entity));
    }

    /**
     * 根据ID删除数据
     * @param id 删除数据的ID
     * @return {@link ResultWrapper}
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "通用delete接口", notes = "restful风格的delete接口，内部删除值使用的是mybatis-plus的removeById接口")
    public ResultWrapper<Boolean> deleteById(@PathVariable @ApiParam("需要删除的资源id") String id) {
        return ResultWrapper.success(service.removeById(id));
    }

    /**
     * 根据ID更新数据
     * @param id ID
     * @param entity 更新实体
     * @return {@link ResultWrapper}
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "通用update接口",
            notes = "restful风格的update接口，内部删除值使用的是mybatis-plus的updateById接口. " +
                    "PS：更新的实体类必须包含被@TableId所注解的字段，否则无法找到需更新的资源（参数id未使用）")
    public ResultWrapper<Boolean> updateById(
            @PathVariable @ApiParam(value = "需要更新的资源id(未使用)", hidden = true) String id,
            @RequestBody @ApiParam(value = "需要更新的资源实体类（PS：实体类中必须包含被@TableId所注解的字段，" +
                    "否则无法找到需更新的资源）") T entity
    ) {
        return ResultWrapper.success(service.updateById(entity));
    }
}
