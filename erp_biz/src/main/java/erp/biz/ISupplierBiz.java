package erp.biz;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import erp.entity.Supplier;
/**
 * 供应商业务逻辑层接口
 * @author Administrator
 *
 */
public interface ISupplierBiz extends IBaseBiz<Supplier>{
	//条件查询
	List<Supplier> getList(Supplier supplier);
	//导出
	void export(OutputStream os,Supplier supplier);
	//导入
	void doImport(InputStream is) throws IOException;
}

