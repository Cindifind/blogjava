package com.example.demo.util;

import com.google.protobuf.Message;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProtoReflectConverter {

    /**
     * POJO → Proto
     */
    @SuppressWarnings("unchecked")
    public static <P extends Message, T> P pojoToProto(T pojo, Class<P> protoClass) {
        if (pojo == null || protoClass == null) return null;

        try {
            Method newBuilderMethod = protoClass.getMethod("newBuilder");
            Message.Builder builder = (Message.Builder) newBuilderMethod.invoke(null);

            Field[] fields = pojo.getClass().getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                String name = f.getName();
                String cap = capitalize(name);

                try {
                    Method getter = pojo.getClass().getMethod("get" + cap);
                    Object value = getter.invoke(pojo);
                    if (value == null) continue;

                    Method setter = builder.getClass().getMethod("set" + cap, f.getType());
                    setter.invoke(builder, value);
                } catch (NoSuchMethodException e) {
                    // 字段不匹配，跳过
                }
            }
            return (P) builder.build();
        } catch (Exception e) {
            throw new RuntimeException("POJO → Proto 反射转换失败: " + protoClass.getSimpleName(), e);
        }
    }

    /**
     * Proto → POJO
     */
    public static <P extends Message, T> T protoToPojo(P proto, Class<T> pojoClass) {
        if (proto == null || pojoClass == null) return null;

        try {
            T pojo = pojoClass.getDeclaredConstructor().newInstance();
            Field[] fields = pojoClass.getDeclaredFields();

            for (Field f : fields) {
                f.setAccessible(true);
                String name = f.getName();
                String cap = capitalize(name);

                try {
                    Method protoGetter = proto.getClass().getMethod("get" + cap);
                    Object value = protoGetter.invoke(proto);

                    Method setter = pojoClass.getMethod("set" + cap, f.getType());
                    setter.invoke(pojo, value);
                } catch (NoSuchMethodException e) {
                    // 字段不匹配，跳过
                }
            }
            return pojo;
        } catch (Exception e) {
            throw new RuntimeException("Proto → POJO 反射转换失败: " + pojoClass.getSimpleName(), e);
        }
    }

    /**
     * ✅ 新增：POJO List → Proto List Message
     * 例如：List<MusicInfoPojo> → MusicInfoList
     *
     * @param pojoList        POJO列表
     * @param protoListClass  Proto List Message的Class（如MusicInfoList.class）
     * @param protoItemClass  Proto Item的Class（如MusicInfo.class）
     * @param repeatedFieldName Proto中repeated字段的名称（如"items"）
     */
    @SuppressWarnings("unchecked")
    public static <P extends Message, I extends Message, T> P pojoListToProtoList(
            List<T> pojoList,
            Class<P> protoListClass,
            Class<I> protoItemClass,
            String repeatedFieldName) {

        if (pojoList == null || pojoList.isEmpty()) {
            return createEmptyProtoList(protoListClass);
        }

        try {
            // 1. 创建Proto List的Builder
            Method newBuilderMethod = protoListClass.getMethod("newBuilder");
            Message.Builder listBuilder = (Message.Builder) newBuilderMethod.invoke(null);

            // 2. 转换每个POJO为Proto Item
            List<I> protoItems = pojoList.stream()
                    .map(pojo -> pojoToProto(pojo, protoItemClass))
                    .collect(Collectors.toList());

            // 3. 找到addAll方法并调用
            String addAllMethodName = "addAll" + capitalize(repeatedFieldName);
            Method addAllMethod = listBuilder.getClass().getMethod(addAllMethodName, Iterable.class);
            addAllMethod.invoke(listBuilder, protoItems);

            return (P) listBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException("POJO List → Proto List 转换失败: " + protoListClass.getSimpleName(), e);
        }
    }

    /**
     * ✅ 新增：Proto List Message → POJO List
     * 例如：MusicInfoList → List<MusicInfoPojo>
     *
     * @param protoList       Proto List Message实例
     * @param pojoClass       POJO的Class
     * @param repeatedFieldName Proto中repeated字段的名称（如"items"）
     */
    @SuppressWarnings("unchecked")
    public static <P extends Message, T> List<T> protoListToPojoList(
            P protoList,
            Class<T> pojoClass,
            String repeatedFieldName) {

        if (protoList == null) return new ArrayList<>();

        try {
            // 1. 获取Proto中的repeated字段列表
            String getListMethodName = "get" + capitalize(repeatedFieldName) + "List";
            Method getListMethod = protoList.getClass().getMethod(getListMethodName);
            List<? extends Message> protoItems = (List<? extends Message>) getListMethod.invoke(protoList);

            // 2. 转换每个Proto Item为POJO
            return protoItems.stream()
                    .map(item -> protoToPojo(item, pojoClass))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Proto List → POJO List 转换失败: " + protoList.getClass().getSimpleName(), e);
        }
    }

    /**
     * ✅ 新增：创建空的Proto List（辅助方法）
     */
    @SuppressWarnings("unchecked")
    private static <P extends Message> P createEmptyProtoList(Class<P> protoListClass) {
        try {
            Method newBuilderMethod = protoListClass.getMethod("newBuilder");
            Message.Builder builder = (Message.Builder) newBuilderMethod.invoke(null);
            return (P) builder.build();
        } catch (Exception e) {
            throw new RuntimeException("创建空Proto List失败: " + protoListClass.getSimpleName(), e);
        }
    }

    /**
     * ✅ 新增：便捷方法 - 不需要指定repeated字段名的版本
     * 假设Proto List中只有一个repeated字段
     */
    @SuppressWarnings("unchecked")
    public static <P extends Message, I extends Message, T> P pojoListToProtoListAuto(
            List<T> pojoList,
            Class<P> protoListClass,
            Class<I> protoItemClass) {

        try {
            // 查找唯一的repeated字段
            String repeatedFieldName = findSingleRepeatedField(protoListClass);
            return pojoListToProtoList(pojoList, protoListClass, protoItemClass, repeatedFieldName);
        } catch (Exception e) {
            throw new RuntimeException("自动检测repeated字段失败", e);
        }
    }

    /**
     * ✅ 新增：查找Proto中唯一的repeated字段
     */
    private static String findSingleRepeatedField(Class<? extends Message> protoListClass) {
        try {
            // 获取newBuilder方法
            Method newBuilderMethod = protoListClass.getMethod("newBuilder");
            Message.Builder builder = (Message.Builder) newBuilderMethod.invoke(null);

            // 获取所有方法
            Method[] methods = builder.getClass().getMethods();

            List<String> repeatedMethods = new ArrayList<>();
            for (Method method : methods) {
                // repeated字段的setter通常以"addAll"开头
                if (method.getName().startsWith("addAll") && method.getParameterCount() == 1) {
                    repeatedMethods.add(method.getName().substring(6)); // 去掉"addAll"
                }
            }

            if (repeatedMethods.size() == 1) {
                return decapitalize(repeatedMethods.get(0));
            } else if (repeatedMethods.size() > 1) {
                throw new RuntimeException("Proto List中有多个repeated字段，请明确指定字段名");
            } else {
                throw new RuntimeException("Proto List中没有找到repeated字段");
            }
        } catch (Exception e) {
            throw new RuntimeException("查找repeated字段失败", e);
        }
    }

    /** foo → Foo */
    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** Foo → foo */
    private static String decapitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}