package com.ohuang.patchuptate;

import java.lang.reflect.Field;

/**
 * 类拷贝
 * */
public class ClassCopier {
    public interface ClassCopierFilter {
        boolean filterField(Field field);
    }

    public static boolean copyClass(Object src, Object tar) {
        if (src == null || tar == null)
            return false;

        new ClassCopier(src.getClass(), tar.getClass()).copy(src, tar);

        return true;
    }

    public static boolean copyClass(Object src, Object tar, ClassCopierFilter filter) {
        if (src == null || tar == null)
            return false;

        new ClassCopier(src.getClass(), tar.getClass()).setFieldFilter(filter).copy(src, tar);

        return true;
    }

    public static boolean copyClass(Object src, Object tar, final int modifiers) {
        if (src == null || tar == null)
            return false;

        new ClassCopier(src.getClass(), tar.getClass()).setFieldFilter(field -> field.getModifiers() == modifiers)
                .copy(src, tar);

        return true;
    }

    Class<?> _sourceCls;
    Class<?> _targetCls;

    boolean _copySuperclass = true;
    ClassCopierFilter _fieldFilter;

    public ClassCopier(Class<?> cls) {
        _sourceCls = cls;
        _targetCls = cls;
    }

    public ClassCopier(Class<?> src, Class<?> tar) {
        _sourceCls = src;
        _targetCls = tar;
    }

    public Object copy(Object src, Object target) {
        Class<?> currentClass = _sourceCls;
        Class<?> targetClass = _targetCls;

        while (currentClass != null && targetClass != null) {
            if (currentClass == Object.class || target == Object.class) {
                break;
            }

            copy(currentClass, targetClass, src, target);

            if (!_copySuperclass)
                break;

            currentClass = currentClass.getSuperclass();
            targetClass = targetClass.getSuperclass();
        }

        return target;
    }

    void copy(Class<?> cls, Class<?> tar, Object src, Object target) {
        for (Field item : cls.getDeclaredFields()) {
            if (canCopy(item)) {
                try {
                    Field tItem = item;
                    if (!_targetCls.equals(_sourceCls))
                        tItem = tar.getDeclaredField(item.getName());

                    item.setAccessible(true);
                    tItem.setAccessible(true);
                    tItem.set(target, item.get(src));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ClassCopier setSuperclass(boolean flag) {
        _copySuperclass = flag;

        return this;
    }

    public ClassCopier setFieldFilter(ClassCopierFilter filter) {
        _fieldFilter = filter;

        return this;
    }

    boolean canCopy(Field field) {
        if (_fieldFilter == null)
            return true;
        else
            return _fieldFilter.filterField(field);
    }
}
