package erp.biz;

import java.util.List;
/**
 * 公共业务层接口
 * @author Administrator
 *
 * @param <T>
 */
public interface IBaseBiz<T> {
		//获取所有
		List<T> getList();
		//条件查询
		List<T> getList(T t,Object params,int page,int rows);
		//查询总数
		int getTotal(T t,Object params);
		//添加
		void add(T t);
		//删除
		void delete(long id);
		//ID查找
		T find(Long id);
		T find(String id);
		//编辑
		void update(T t);
}
