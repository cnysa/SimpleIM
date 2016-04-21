package com.langsin.im.client;

import java.util.List;

import javax.swing.event.TableModelListener;

import com.langsin.im.model.UserInfo;

 
/**
 *��ʱͨ��ϵͳ ��ʾ���ҵ����û���Ϣ�ı�ģ��
 *�����ҽ����Ϣ�ӷ���������ʱ�������ڽ���Jtable��
 *��ʾ�ҵ����û���Ϣ
 */
public class UserInfoTableMode 
implements javax.swing.table.TableModel{
   //ָ��������������б���� �û������̶߳������
	private  List<UserInfo> users; 
	
	//������ģ�Ͷ���ʱ,�����û��б�
	public UserInfoTableMode(List<UserInfo> ctList){
		this.users=ctList;
	}
     //������ ʵ��TableModel�е�
    public int getRowCount(){
    	return users.size();
    }

   //������ ʵ��TableModel�е�
    public int getColumnCount(){
    	return 2; //yk��,�û���
    }
    //�б��� ʵ��TableModel�е�
    public String getColumnName(int columnIndex){
    	if(columnIndex==0){
    		return "yk��";
    	}else if(columnIndex==1){
        		return "�س�";
    	}else{ return "error";}
    }
    //ÿһ�е��������� ʵ��TableModel�е�
    public Class<?> getColumnClass(int columnIndex){
    	return String.class;
    }
    //��Ԫ���Ƿ�ɱ༭ ʵ��TableModel�е�
    public boolean isCellEditable(int rowIndex, int columnIndex){
    	return false;
    }
    //������ʾʱ������������� ʵ��TableModel�е�
    public Object getValueAt(int rowIndex, int columnIndex){
    	//�ڼ��У����Ƕ����еڼ����̶߳���������û�
    	UserInfo  user=users.get(rowIndex);
    	if(columnIndex==0){
    		return user.getykNum();
    	}else if(columnIndex==1){
        		return user.getNikeName();
    	}else{ return "error"; }
    }
 //�ݲ�ʹ��  
    public void setValueAt(Object a, int r, int c){}
    public void addTableModelListener(TableModelListener l){}
    public void removeTableModelListener(TableModelListener l){}
}