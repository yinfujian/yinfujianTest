THE SPRING FRAMEWORK, release 1.0 M1 (August 2003)
--------------------------------------------------
http://www.springframework.org


1. 介绍

Spring 是一个基于Rod Johnson (Wrox, 2002)在"Expert One-on-One J2EE Design and Development" 上发布的code的J2EE框架

Spring 包含:

* IOC:基于强大的配置管理，应用控制反转原理。使得写代码是快速而且简单。不再有很多单例分散在你的代码库中；不再有混乱的配置文件。在每一个地方都提供优雅且一致的方法
* JDBC:JDBC抽象层提供了一个有意义的异常层次结构（不再从SQLException中提取供应商代码），简化了错误处理，并大大减少了需要编写的代码量。您再也不需要编写另一个finally块来使用JDBC了。
* 事务管理：类似的抽象层用于事务管理，允许使用可插入的事务管理器，并使划分事务变得容易，而无需处理低级问题。其中包括针对JTA和单个JDBC数据源的策略。
* DAO实现支持：在资源持有者、DAO实现支持和事务策略方面与Hibernate和JDO集成。一流的Hibernate支持，具有许多IoC便利功能，解决了许多典型的Hibernate集成问题。
* AOP功能：完全集成到Spring配置管理中。您可以通过AOP启用Spring管理的任何对象，添加声明性事务管理等方面。使用Spring，您可以在没有EJB的情况下进行声明性事务管理。。。即使没有JTA，如果您在Tomcat中使用一个数据库或另一个没有JTA支持的web容器。
* Spring MVC:灵活的MVC web应用程序框架，建立在Spring的核心功能之上。该框架具有高度可配置性，并适应多种视图技术。

您可以在任何J2EE服务器中使用Spring的所有功能，其中大部分也可以在非托管环境中使用。Spring的一个中心焦点是允许不绑定到特定J2EE服务的可重用业务和数据访问对象。
这样的对象可以在有或没有EJB的J2EE环境、独立应用程序、测试环境等中使用，而不会有任何麻烦。

Spring具有分层体系结构。它的所有功能都建立在较低的级别上。因此，您可以在不使用MVC框架或AOP支持的情况下使用JavaBeans配置管理。
但是，如果您使用MVC框架或AOP支持，您会发现它们构建在配置框架上，因此您可以立即应用有关它的知识。

2. 发布信息

Spring Framework是根据Apache软件许可证的条款发布的（请参阅License.txt）。这是第三次向1.0最终版本公开发布。
作为第一个具有里程碑意义的1.0版本，它引入了包名称“org.springframework”，取代了之前的“com.interface21”，后者可以追溯到该书的框架版本。


Spring Framework需要J2SE 1.3和J2EE 1.3（Servlet 2.3、JSP 1.2、JTA 1.0、EJB 2.0）。
请注意，如果不使用Spring的web MVC或EJB支持，J2EE 1.2（Servlet 2.2、JSP 1.1）就足够了。
集成提供了Log4J 1.2、CGLIB 1.0、Hibernate 2.0、JDO 1.0、Caucho的Hessian/Burlap 2.1/3.0、JSTL 1.0、Velocity 1.3等。

发布内容:
* "src" contains the Java source files 源文件
* "dist" contains various Spring Jar files jar文件
* "lib" contains the most important third-party libraries lib中包含重要的第三方依赖
* "docs" contains general and API documentation
* "samples" contains demo application and skeletons samples中提供了demo应用和架构

Latest info is available at the public website: http://www.springframework.org 最新的可用的信息在这个网站
Project info at the SourceForge site: http://sourceforge.net/projects/springframework 开源版本在这

This product includes software developed by the Apache Software Foundation (http://www.apache.org).


3. jar包的分类

“dist”目录包含以下用于应用程序的重叠jar文件。每一个都涉及Spring Framework的典型用法，指定各自的内容和第三方依赖关系。
括号中的库是可选的，即只是某些功能所必需的。

* "spring-beans" (~90 KB)
- 目标：用在小型程序中国
- 内容: bean container, core utilities
- Dependencies: Commons Logging

* "spring-context" (~190 KB)
- Target: basic application context for use outside a J2EE container
- Contents: bean container, utilities, AOP framework, application context, validation framework
- Dependencies: Commons Logging, (Log4J, AOP Alliance, CGLIB)

* "spring-jdbc" (~340 KB)
- Target: application context with transaction framework and JDBC support
- Contents: bean container, utilities, AOP framework, application context, validation framework, transaction framework, JDBC support
- Dependencies: Commons Logging, (Log4J, AOP Alliance, CGLIB; JTA)

* "spring" (~560 KB)
- Target: full application framework for use within a J2EE container (and of course suitable where jar size does not matter)
- Contents: bean container, utilities, AOP framework, EJB support, transaction framework, JDBC support, O/R Mapping support, application context, web application context, validation framework, web MVC framework, remoting support
- Dependencies: Commons Logging, (Log4J, AOP Alliance, CGLIB; JTA; Hibernate, JDO; EJB 2.0, Servlet 2.3, JSP 1.2, JSTL; Velocity, iText, POI; Hessian, Burlap)

注意: 上面列出的第三方库假定J2SE 1.4为基础。对于J2SE 1.3，在分别使用XMLbean定义、JDBC DataSource设置和JNDI查找时，必须添加像Xerces这样的XML解析器、JDBC 2.0标准扩展接口和JNDI。

注意: 要将JSP表达式语言用于Spring的web MVC标记的参数，必须在类路径中提供JSTL（standard.jar）的Jakarta实现。否则，任何JSTL实现都可以。


4. WHERE TO START?

Documentation can be found in the "docs" directory:
* "The Spring Framework - A Lightweight Container" spring 框架，介绍
* "Data Access and Transaction Abstraction with the Spring Framework" 数据库和事务管理
* "Container Resources vs Local Resources" 容器资源和本地资源
* "Web MVC with the Spring Framework" mvc
* "Developing a Spring Framework MVC application step-by-step" spring mvc 一步一步开发

Documented application skeletons can be found in "samples/skeletons":
webapp demo代码
* "webapp-minimal"
* "webapp-typical"
* "webapp-hibernate"
* "webapp-aop"

演示应用程序“国家”和“宠物诊所”可以分别在“samples/Countries”和“samples/Petclinic”中找到（带有自己的自述文件.txt）。

“Expert One-on-One J2EE Design and Development”详细讨论了Spring的许多设计思想。注：本书中的代码示例指的是本书附带的原始框架版本。因此，可能需要对它们进行调整，以便与当前的Spring版本一起使用。

