#include <gtest/gtest.h>
#include <jni.h>
#include <qi/type/dynamicobjectbuilder.hpp>
#include <qi/property.hpp>
#include <jnitools.hpp>
#include <jobjectconverter.hpp>
#include <object.hpp>
#include <objectbuilder.hpp>


qiLogCategory("qimessaging.jni.test");


int main(int argc, char** argv)
{
  // Avoid making a qi::Application. Real Java apps cannot do it.
  qi::log::addFilter("qimessaging.jni", qi::LogLevel_Debug);
  ::testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}


class QiJNI: public ::testing::Test
{
protected:
  static void SetUpTestCase()
  {
    JavaVMOption options[1];
    JavaVMInitArgs vm_args;
    long status;

    char classPathDefinition[] = "-Djava.class.path=.";
    options[0].optionString = classPathDefinition;
    memset(&vm_args, 0, sizeof(vm_args));
    vm_args.version = JNI_VERSION_1_6;
    vm_args.nOptions = 1;
    vm_args.options = options;
    status = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);

    if (status == JNI_ERR)
      throw std::runtime_error("Failed to set a JVM up");
    jvm->AttachCurrentThread((void**)&env, nullptr);

    // Real Java apps will always call this when loading the library.
    JNI_OnLoad(jvm, nullptr);

    Java_com_aldebaran_qi_EmbeddedTools_initTypeSystem(env, jclass{});
  }

  static void TearDownTestCase()
  {
    if (jvm)
      jvm->DestroyJavaVM();
  }

  static JNIEnv* env;
  static JavaVM* jvm;
};

JNIEnv* QiJNI::env = nullptr;
JavaVM* QiJNI::jvm = nullptr;

template <typename T>
qi::AnyObject makeObjectWithProperty(const std::string& propertyName, qi::Property<T>& property)
{
  qi::DynamicObjectBuilder objectBuilder;
  objectBuilder.advertiseProperty(propertyName, &property);
  return objectBuilder.object();
}

TEST_F(QiJNI, setProperty)
{
  const int initialValue = 12;
  const int newValue = 42;
  qi::Property<int> property{initialValue};

  const std::string propertyName = "serendipity";
  auto object = makeObjectWithProperty(propertyName, property);
  auto objectPtr = &object;

  qi::jni::JNIAttach attach{env};
  auto futureAddress = Java_com_aldebaran_qi_AnyObject_setProperty(
        env, jobject{},
        reinterpret_cast<jlong>(objectPtr),
        qi::jni::toJstring(propertyName),
        JObject_from_AnyValue(qi::AnyValue{newValue}.asReference()));

  auto future = reinterpret_cast<qi::Future<qi::AnyValue>*>(futureAddress);
  auto status = future->waitFor(qi::MilliSeconds{200});
  ASSERT_EQ(qi::FutureState_FinishedWithValue, status);
  ASSERT_EQ(newValue, object.property<int>(propertyName).value());
}

TEST_F(QiJNI, futureErrorIfSetPropertyThrows)
{
  using CustomException = std::exception;
  const int initialValue = 12;
  const int newValue = 42;
  qi::Property<int> property{initialValue, qi::Property<int>::Getter{}, [this](int&, const int&)->bool
  {
    throw CustomException{};
  }};

  const std::string propertyName = "serendipity";
  auto object = makeObjectWithProperty(propertyName, property);
  auto objectPtr = &object;

  qi::jni::JNIAttach attach{env};
  auto futureAddress = Java_com_aldebaran_qi_AnyObject_setProperty(
        env, jobject{},
        reinterpret_cast<jlong>(objectPtr),
        qi::jni::toJstring(propertyName),
        JObject_from_AnyValue(qi::AnyValue{newValue}.asReference()));

  auto future = reinterpret_cast<qi::Future<qi::AnyValue>*>(futureAddress);
  auto status = future->waitFor(qi::MilliSeconds{200});
  ASSERT_EQ(qi::FutureState_FinishedWithError, status);
  ASSERT_EQ(initialValue, object.property<int>(propertyName).value());
}


TEST_F(QiJNI, dynamicObjectBuilderAdvertiseMethodVoidVoid)
{
  // Object.notify() is a typical example of function taking and returning nothing.
  const auto javaObjectClassName = "java/lang/Object";
  const auto javaObjectClass = env->FindClass(javaObjectClassName);
  const auto javaObjectConstructor = env->GetMethodID(javaObjectClass, "<init>", "()V");
  const auto javaObject = env->NewObject(javaObjectClass, javaObjectConstructor);

  // Let's make a Qi Object calling that method.
  const auto objectBuilderAddress = Java_com_aldebaran_qi_DynamicObjectBuilder_create(env, nullptr);
  env->ExceptionClear();
  Java_com_aldebaran_qi_DynamicObjectBuilder_advertiseMethod(
        env, jobject{},
        objectBuilderAddress,
        qi::jni::toJstring("notify::v()"), javaObject, // 'v' stands for "void"
        qi::jni::toJstring(javaObjectClassName),
        qi::jni::toJstring("Whatever"));
  ASSERT_FALSE(env->ExceptionCheck());
}
