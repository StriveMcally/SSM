package com.yt.commons.batch;

import com.yt.commons.IBatchExecutor;
import com.yt.commons.exceptions.CustomException;
import com.yt.commons.utils.ApplicationContextUtil;
import com.yt.mybatis.model.BaseMapper;
import com.yt.mybatis.model.BaseModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量插入数据到数据库
 * Created by yt on 2016/8/26.
 */

public class BatchExecutor<T extends BaseModel> implements IBatchExecutor<T> {



    private SqlSessionFactory sqlSessionFactory ;

    private BaseMapper mapper;

    private  int batchSize=500;
    public BatchExecutor(){
        this.sqlSessionFactory= ApplicationContextUtil.getBeanByClass(SqlSessionFactory.class) ;
    }

    public BatchExecutor(BaseMapper mapper){
        this.mapper=mapper;
        this.sqlSessionFactory= ApplicationContextUtil.getBeanByClass(SqlSessionFactory.class) ;
    }

    public BatchExecutor(BaseMapper mapper,int batchSize){
        this.mapper=mapper;
        this.batchSize=batchSize;
        this.sqlSessionFactory= ApplicationContextUtil.getBeanByClass(SqlSessionFactory.class) ;
    }




    public int batchInsert(List<T> list) {
        int insertNumber=0;
        sqlSessionFactory.getConfiguration().addMapper(BaseMapper.class);
        try(SqlSession sqlSession=sqlSessionFactory.openSession(ExecutorType.BATCH,false)){
            int listSize=list.size();
            List<T> insertList=new ArrayList<>();
            for (int i=0;i<listSize;i++) {
                insertNumber++;
                T t=list.get(i) ;
                if(null==this.mapper){
                    Class <?> tClass=getMapperClass(t.getClass().getName());
                    if(null==tClass){
                        throw new CustomException(50001,t.getClass().getName()+"实体对应的Mapper加载类失败！");
                    }
                    this.mapper=(BaseMapper)sqlSession.getMapper(tClass);
                }
                insertList.add(t);
                if(listSize==1 || (i!=0 && (i%this.batchSize==0 || i==listSize-1))){
                    /*this.mapper.insertBatch(insertList);
                    sqlSession.commit();
                    sqlSession.clearCache();
                    insertList.clear();*/
                }
            };
        }
        return insertNumber;
    }

    //当没有初始化BaseMapper时，通过model获得对应mapper的类路径;要求MBG生成的model、mapper文件的相对路径默认没有改变。
    private Class<?>  getMapperClass(String s){
        String mapperClass= StringUtils.replaceOnce(s, ".model.", ".mapper.")+"Mapper";
        Class<?> tClass=null;
        try {
            tClass=Class.forName(mapperClass);
        } catch (ClassNotFoundException ex){
              throw new CustomException(50033,"路径:"+mapperClass+"下加载类失败！");
        }
        return tClass;
    }


    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public void setMapper(BaseMapper mapper) {
        this.mapper = mapper;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
