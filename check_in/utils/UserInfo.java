package com.example.check_in.utils;

import com.bin.david.form.annotation.SmartColumn;
import com.bin.david.form.annotation.SmartTable;

@SmartTable(name = "参会人员信息")
public class UserInfo {
    @SmartColumn(id = 0, name = "姓名")
    private String name;
    @SmartColumn(id = 1, name = "电话")
    private String telephone;
    @SmartColumn(id = 2, name = "学院")
    private String college;
    @SmartColumn(id = 3, name = "学号")
    private String number;
    @SmartColumn(id = 4, name = "状态", autoMerge = true)
    private String flag;

    public UserInfo(String name, String telephone, String college, String number, String flag) {
        this.name = name;
        this.telephone = telephone;
        this.college = college;
        this.number = number;
        this.flag = flag;
    }

    public String getName() {
        return name;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getCollege() {
        return college;
    }

    public String getNumber() {
        return number;
    }

    public String getFlag() {
        return flag;
    }
}
