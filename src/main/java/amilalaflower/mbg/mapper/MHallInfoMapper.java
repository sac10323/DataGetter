package amilalaflower.mbg.mapper;

import amilalaflower.mbg.entity.MHallInfo;
import amilalaflower.mbg.entity.MHallInfoExample;
import amilalaflower.mbg.entity.MHallInfoKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MHallInfoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table M_HALL_INFO
     *
     * @mbggenerated Sun Nov 13 19:42:21 JST 2016
     */
    int countByExample(MHallInfoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table M_HALL_INFO
     *
     * @mbggenerated Sun Nov 13 19:42:21 JST 2016
     */
    int deleteByExample(MHallInfoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table M_HALL_INFO
     *
     * @mbggenerated Sun Nov 13 19:42:21 JST 2016
     */
    int deleteByPrimaryKey(MHallInfoKey key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table M_HALL_INFO
     *
     * @mbggenerated Sun Nov 13 19:42:21 JST 2016
     */
    int insert(MHallInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table M_HALL_INFO
     *
     * @mbggenerated Sun Nov 13 19:42:21 JST 2016
     */
    int insertSelective(MHallInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table M_HALL_INFO
     *
     * @mbggenerated Sun Nov 13 19:42:21 JST 2016
     */
    List<MHallInfo> selectByExample(MHallInfoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table M_HALL_INFO
     *
     * @mbggenerated Sun Nov 13 19:42:21 JST 2016
     */
    MHallInfo selectByPrimaryKey(MHallInfoKey key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table M_HALL_INFO
     *
     * @mbggenerated Sun Nov 13 19:42:21 JST 2016
     */
    int updateByExampleSelective(@Param("record") MHallInfo record, @Param("example") MHallInfoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table M_HALL_INFO
     *
     * @mbggenerated Sun Nov 13 19:42:21 JST 2016
     */
    int updateByExample(@Param("record") MHallInfo record, @Param("example") MHallInfoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table M_HALL_INFO
     *
     * @mbggenerated Sun Nov 13 19:42:21 JST 2016
     */
    int updateByPrimaryKeySelective(MHallInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table M_HALL_INFO
     *
     * @mbggenerated Sun Nov 13 19:42:21 JST 2016
     */
    int updateByPrimaryKey(MHallInfo record);
}