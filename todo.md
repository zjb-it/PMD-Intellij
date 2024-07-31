# 实时检测
https://plugins.jetbrains.com/docs/intellij/code-inspections.html#inspection-implementation-java-class

过早定义变量
net.sourceforge.pmd.lang.java.rule.codestyle.PrematureDeclarationRule


[//]: # (pmd不支持)
【强制】不能使用过时的类或方法。

（十二）【强制】构造方法里面禁止加入任何业务逻辑，如果有初始化逻辑，请放在 init 方法中

（十三）【强制】POJO 类必须写 toString 方法。使用 IDE 中的工具：source> generate toString时，如果继承了另一个 POJO 类，注意在前面加一下 super.toString。

（二）【强制】当 switch 括号内的变量类型为 String 并且此变量为外部参数时，必须先进行 null判断。
//SwitchStatement[not(VariableAccess/@Name = ..//..//MethodCall[@MethodName="nonNull"]/ArgumentList/VariableAccess/@Name
or VariableAccess/@Name = ..//../InfixExpression[@Operator = '!='][NullLiteral]/VariableAccess/@Name
or VariableAccess/@Name = ..//..//MethodCall[@MethodName="isNotNull"]/ArgumentList/VariableAccess/@Name)]

未解决变量=string的问题



//VariableId[@Name = //SwitchStatement/VariableAccess/@Name and ../MethodCall[pmd-java:typeIsExactly('java.lang.String')]]

```java
package com.intellij.plugins.bodhi.pmd.lang.java.rule.comment;

import java.util.Objects;

public class SwitchString {
    public static void main(String[] args) {
        String param1="33";
        method(null);
    }

    public static void method(String param) {
        String param1="";
        param1=method2();
        String param2=method2();
        if (Objects.nonNull(param1)) {
            switch (param1) {
// 肯定不是进入这里
                case "sth":
                    System.out.println("it's sth");
                    break;
// 也不是进入这里
                case "null":
                    System.out.println("it's null");
                    break;
// 也不是进入这里
                default:
                    System.out.println("default");
            }
        }//
    }
    public static String method2(){
        return "";
    }

} 
```






JavaAstUtils
TypeTestUtil
JavaRuleUtil 