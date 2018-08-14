package erp.biz.impl;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import erp.biz.ISupplierBiz;
import erp.biz.exception.ERPException;
import erp.dao.ISupplierDao;
import erp.entity.Supplier;
/**
 * 供应商业务逻辑类
 * @author Administrator
 *
 */
public class SupplierBiz extends BaseBiz<Supplier> implements ISupplierBiz {

	private ISupplierDao supplierDao;
	
	public void setSupplierDao(ISupplierDao supplierDao) {
		this.supplierDao = supplierDao;
		super.setBaseDao(this.supplierDao);
	}
	/**
	 * 条件查询
	 */
	@Override
	public List<Supplier> getList(Supplier supplier) {
		return supplierDao.getList(supplier);
	}
	/**
	 * 导出
	 */
	@Override
	public void export(OutputStream os, Supplier supplier) {
		Subject subject=SecurityUtils.getSubject();
		if(supplier.getType().equals(Supplier.TYPE_CUSTOMER)){
			if(!subject.isPermitted("客户管理")){
				throw new ERPException("权限不足");
			}
		}else if(supplier.getType().equals(Supplier.TYPE_SUPPLIER)){
			if(!subject.isPermitted("供应商管理")){
				throw new ERPException("权限不足");
			}
		}else{
			throw new ERPException("非法参数");
		}	
		List<Supplier> list=getList(supplier);
		//工作簿
		HSSFWorkbook wb=new HSSFWorkbook();
		//工作表
		HSSFSheet sheet=null;
		//设置表名
		if(supplier.getType().equals(Supplier.TYPE_CUSTOMER)){
			sheet=wb.createSheet("客户");
		}else{
			sheet=wb.createSheet("供应商");
		}
		//首行列名
		String[] rowname={"名称","地址","联系人","电话","邮箱"};
		//每列宽度
		int[] width={5000,8000,4000,5000,8000};
		//第一行
		HSSFRow row=sheet.createRow(0);
		//单元格
		HSSFCell cell=null;
		for(int i=0;i<rowname.length;i++){
			cell=row.createCell(i);
			cell.setCellValue(rowname[i]);
			sheet.setColumnWidth(i, width[i]);
		}
		//第二行开始
		int rownum=1;
		for(Supplier s:list){
			row=sheet.createRow(rownum);
			row.createCell(0).setCellValue(s.getName());
			row.createCell(1).setCellValue(s.getAddress());
			row.createCell(2).setCellValue(s.getContact());
			row.createCell(3).setCellValue(s.getTele());
			row.createCell(4).setCellValue(s.getEmail());
			rownum++;
		}
		
		try {
			wb.write(os);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				wb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 导入
	 * @throws IOException 
	 */
	@Override
	public void doImport(InputStream is) throws IOException {
		Subject subject=SecurityUtils.getSubject();
		HSSFWorkbook wb=null;
		try {
			wb=new HSSFWorkbook(is);
			HSSFSheet sheet=wb.getSheetAt(0);
			String type="";
			if(sheet.getSheetName().equals("供应商")){
				type=Supplier.TYPE_SUPPLIER;
				if(!subject.isPermitted("供应商管理")){
					throw new ERPException("权限不足");
				}
			}else if(sheet.getSheetName().equals("客户")){
				type=Supplier.TYPE_CUSTOMER;
				if(!subject.isPermitted("客户管理")){
					throw new ERPException("权限不足");
				}
			}else{
				throw new ERPException("表格名称不正确");
			}
			Supplier s=null;
			for(int i=1;i<=sheet.getLastRowNum();i++){
				s=new Supplier();
				Supplier temp=supplierDao.findByName(sheet.getRow(i).getCell(0).getStringCellValue());	
				//存在 持久化该对象
				if(temp!=null){
					s=temp;
				}
				s.setAddress(sheet.getRow(i).getCell(1).getStringCellValue());
				s.setContact(sheet.getRow(i).getCell(2).getStringCellValue());
				s.setTele(sheet.getRow(i).getCell(3).getStringCellValue());
				s.setEmail(sheet.getRow(i).getCell(4).getStringCellValue());
				//不存在 添加
				if(temp==null){
					s.setName(sheet.getRow(i).getCell(0).getStringCellValue());
					s.setType(type);
					supplierDao.add(s);
				}			
			}
		} finally{
			if(wb!=null){
				try {
					wb.close();
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		}
		
	}
	
	
}
