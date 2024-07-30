# 实时检测
https://plugins.jetbrains.com/docs/intellij/code-inspections.html#inspection-implementation-java-class

过早定义变量
net.sourceforge.pmd.lang.java.rule.codestyle.PrematureDeclarationRule


[//]: # (pmd不支持)
【强制】不能使用过时的类或方法。

（十二）【强制】构造方法里面禁止加入任何业务逻辑，如果有初始化逻辑，请放在 init 方法中

（十三）【强制】POJO 类必须写 toString 方法。使用 IDE 中的工具：source> generate toString时，如果继承了另一个 POJO 类，注意在前面加一下 super.toString。


JavaAstUtils
TypeTestUtil
JavaRuleUtil 