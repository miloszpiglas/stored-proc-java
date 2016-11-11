/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.mpiglas.dbproc.postgres.jpa;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;

/**
 *
 * @author milosz
 */
@SqlResultSetMapping(name = "GenRowRecord", entities =
{
    @EntityResult(entityClass = StrIntRecord.class, fields =
    {
        @FieldResult(column = "num", name = "num"),
        @FieldResult(column = "str", name = "str")
    })
})
@Entity
public class StrIntRecord
{

    private String str;
    @Id
    private Integer num;

    public StrIntRecord()
    {

    }

    public StrIntRecord(String str, Integer num)
    {
        this.str = str;
        this.num = num;
    }

    public String getStr()
    {
        return str;
    }

    public void setStr(String str)
    {
        this.str = str;
    }

    public Integer getNum()
    {
        return num;
    }

    public void setNum(Integer num)
    {
        this.num = num;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final StrIntRecord other = (StrIntRecord) obj;
        if (!Objects.equals(this.str, other.str))
        {
            return false;
        }
        if (!Objects.equals(this.num, other.num))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "StrIntRecord{" + "str=" + str + ", num=" + num + '}';
    }

}
