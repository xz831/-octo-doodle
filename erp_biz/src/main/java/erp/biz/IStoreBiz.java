package erp.biz;
import java.util.List;

import erp.entity.Store;
/**
 * 仓库业务逻辑层接口
 * @author Administrator
 *
 */
public interface IStoreBiz extends IBaseBiz<Store>{
	
	//重载条件查询
	List<Store> getList(Store s);

}

