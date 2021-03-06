/*
**
** Author(s):
**  - Pierre ROULLON <proullon@aldebaran-robotics.com>
**
** Copyright (C) 2013 Aldebaran Robotics
** See COPYING for the license
*/

#include <stdexcept>
#include <sstream>
#include <qi/log.hpp>
#include <jnitools.hpp>
#include <tuple_jni.hpp>

JNITuple::JNITuple(jobject obj)
{
  javaVirtualMachine->GetEnv((void**) &_env, QI_JNI_MIN_VERSION);
  _obj = obj;
}

int JNITuple::size()
{
  jmethodID mid = _env->GetMethodID(cls_tuple, "size", "()I");

  if (!mid)
  {
    qiLogFatal("qimessaging.jni") << "JNITuple : Cannot call method size()I";
    throw std::runtime_error("JNITuple : Cannot call method size()I");
  }

  return _env->CallIntMethod(_obj, mid);
}

jobject JNITuple::get(int index)
{
  jmethodID mid = _env->GetMethodID(cls_tuple, "get", "(I)Ljava/lang/Object;");

  if (!mid)
  {
    qiLogFatal("qimessaging.jni") << "JNITuple : Cannot call method get(I)Ljava/lang/Object;";
    throw std::runtime_error("JNITuple : Cannot call method get(I)Ljava/lang/Object;");
  }

  return _env->CallObjectMethod(_obj, mid, index);
}

void JNITuple::set(int index, jobject obj)
{
  jmethodID mid = _env->GetMethodID(cls_tuple, "set", "(ILjava/lang/Object;)V");

  if (!mid)
  {
    qiLogFatal("qimessaging.jni") << "JNITuple : Cannot call method set(ILjava/lang/Object;)V";
    throw std::runtime_error("JNITuple : Cannot call method set(ILjava/lang/Object;)V");
  }

  _env->CallVoidMethod(_obj, mid, index, obj);
}

jobject JNITuple::object()
{
  return _obj;
}
