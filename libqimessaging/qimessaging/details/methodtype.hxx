#pragma once
/*
**  Copyright (C) 2012 Aldebaran Robotics
**  See COPYING for the license
*/

#ifndef _QIMESSAGING_DETAILS_METHODTYPE_HXX_
#define _QIMESSAGING_DETAILS_METHODTYPE_HXX_

namespace qi
{
  namespace detail
  {
    // Convert method signature to function signature by putting
    // "class instance pointer" type as first argument
    // ex: int (Foo::*)(int) => int (*)(Foo*, int)
    template<typename F>
    struct MethodToFunctionTrait
    {
      // Result type
      typedef typename ::boost::function_types::result_type<F>::type RetType;
      // All arguments including class pointer
      typedef typename ::boost::function_types::parameter_types<F>::type ArgsType;
      // Class ref type
      typedef typename ::boost::mpl::front<ArgsType>::type ClassRefType;
      // Convert it to ptr type
      typedef typename boost::add_pointer<typename boost::remove_reference<ClassRefType>::type>::type ClassPtrType;
      // Argument list, changing ClassRef to ClassPtr
      typedef typename boost::mpl::push_front<
        typename ::boost::mpl::pop_front<ArgsType>::type,
      ClassPtrType>::type ArgsTypeFixed;
      // Push result type in front
      typedef typename ::boost::mpl::push_front<ArgsTypeFixed, RetType>::type FullType;
      // Synthetise result function type
      typedef typename ::boost::function_types::function_type<FullType>::type type;
      // Compute bound type
      typedef typename boost::mpl::push_front<
        typename boost::mpl::pop_front<ArgsType>::type, RetType>::type BoundTypeSeq;
      // Synthetise method type
      typedef typename ::boost::function_types::function_type<BoundTypeSeq>::type BoundType;
    };
  } // namespace detail

  template<typename T>
  class MethodTypeImpl:
    public virtual MethodType,
    public virtual FunctionTypeImpl<T>
  {
    void* call(void* method, void* object,
      const std::vector<void*>& args)
    {
      std::vector<void*> nargs;
      nargs.reserve(args.size()+1);
      nargs.push_back(object);
      nargs.insert(nargs.end(), args.begin(), args.end());
      return FunctionTypeImpl<T>::call(method, nargs);
    }
    GenericValue call(void* method, GenericValue object,
      const std::vector<GenericValue>& args)
    {
      std::vector<GenericValue> nargs;
      nargs.reserve(args.size()+1);
      nargs.push_back(object);
      nargs.insert(nargs.end(), args.begin(), args.end());
      return FunctionType::call(method, nargs);
    }
  };

  template<typename T>
  MethodType* methodTypeOf()
  {
    static MethodTypeImpl<T> result;
    return &result;
  }
  template<typename M>
  GenericMethod makeGenericMethod(const M& method)
  {
    // convert M to a boost::function with an extra arg object_type
    typedef typename detail::MethodToFunctionTrait<M>::type Linearized;
    boost::function<Linearized> f = method;

    GenericFunction fv = makeGenericFunction(f);
    GenericMethod result;
    result.value = fv.value;
    result.type = methodTypeOf<Linearized>();
    return result;
  }

  inline GenericFunction GenericMethod::toGenericFunction()
  {
    GenericFunction res;
    res.type = dynamic_cast<FunctionType*>(type);
    res.value = value;
    return res;
  }

} // namespace qi

#endif  // _QIMESSAGING_DETAILS_METHODTYPE_HXX_