package com.xinzhi.furniture.mall.db.dao;

import com.xinzhi.furniture.mall.db.domain.LitemallSystem;
import com.xinzhi.furniture.mall.db.domain.LitemallSystemExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LitemallSystemMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    long countByExample(LitemallSystemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    int deleteByExample(LitemallSystemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    int insert(LitemallSystem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    int insertSelective(LitemallSystem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    LitemallSystem selectOneByExample(LitemallSystemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    LitemallSystem selectOneByExampleSelective(@Param("example") LitemallSystemExample example, @Param("selective") LitemallSystem.Column ... selective);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    List<LitemallSystem> selectByExampleSelective(@Param("example") LitemallSystemExample example, @Param("selective") LitemallSystem.Column ... selective);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    List<LitemallSystem> selectByExample(LitemallSystemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    LitemallSystem selectByPrimaryKeySelective(@Param("id") Integer id, @Param("selective") LitemallSystem.Column ... selective);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    LitemallSystem selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    LitemallSystem selectByPrimaryKeyWithLogicalDelete(@Param("id") Integer id, @Param("andLogicalDeleted") boolean andLogicalDeleted);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") LitemallSystem record, @Param("example") LitemallSystemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") LitemallSystem record, @Param("example") LitemallSystemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(LitemallSystem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(LitemallSystem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    int logicalDeleteByExample(@Param("example") LitemallSystemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_system
     *
     * @mbg.generated
     */
    int logicalDeleteByPrimaryKey(Integer id);
}