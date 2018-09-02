package top.huzhurong.aop.advisor.transaction.manager;

import lombok.Getter;
import lombok.Setter;
import top.huzhurong.aop.advisor.transaction.TransactionAttrSource;
import top.huzhurong.aop.advisor.transaction.TransactionManager;
import top.huzhurong.aop.advisor.transaction.definition.TransactionDefinition;
import top.huzhurong.aop.advisor.transaction.definition.TransactionStatus;
import top.huzhurong.aop.invocation.AbstractInvocation;
import top.huzhurong.aop.invocation.Invocation;

import java.sql.SQLException;

/**
 * @author luobo.cs@raycloud.com
 * @since 2018/9/1
 */
public class TransactionInterceptor {

    @Getter
    @Setter
    private TransactionManager transactionManager;

    public TransactionInterceptor(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public Object doTransaction(Invocation invocation, Object object) throws SQLException {
        AbstractInvocation abstractInvocation = (AbstractInvocation) invocation;
        TransactionDefinition definition = TransactionAttrSource.getDefinition(abstractInvocation.getMethod());
        //获取事务
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        Object invoke = null;
        try {
            invoke = abstractInvocation.getMethod().invoke(object, abstractInvocation.getArgs());
            commitTransactionAfterReturning(transaction);
        } catch (Throwable e) {
            completeTransactionAfterThrowing(transaction);
            e.printStackTrace();
        } finally {
            cleanupTransactionInfo();
        }
        return invoke;
    }

    //事务成功完成，开始提交
    private void commitTransactionAfterReturning(TransactionStatus transaction) throws SQLException {
        transactionManager.commit(transaction);
    }

    //移除绑定在线程上的事务属性
    private void cleanupTransactionInfo() {
        SyncTransactionUtil.clear();
        ConnectionManager.remove();
    }

    /**
     * 事务处理过程当中抛出的异常，可能提交，也可能会滚，取决于配置信息
     */
    private void completeTransactionAfterThrowing(TransactionStatus transaction) throws SQLException {
        transactionManager.rollback(transaction);
    }

}