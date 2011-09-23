#pragma once
/*
* AUTOGENERATED CODE, DO NOT EDIT
*
*  Author(s):
*  - Cedric GESTES <gestes@aldebaran-robotics.com>
*
*  Copyright (C) 2010 Aldebaran Robotics
*/


#ifndef _QI_FUNCTORS_DETAIL_VOIDMEMBERFUNCTOR_HXX_
#define _QI_FUNCTORS_DETAIL_VOIDMEMBERFUNCTOR_HXX_

# include <qi/functors/functor.hpp>

namespace qi {
namespace detail {


  template < typename C>
  class MemberFunctor_0< C, void> : public Functor
  {
  public:
    typedef void(C::*FunctionType) ();

    MemberFunctor_0(C *pObject, FunctionType pFunction)
      : fObject(pObject),
        fFunction(pFunction) {
    }

    void operator()() {
      (fObject->*fFunction)();
    }

    void call(qi::serialization::Message &params, qi::serialization::Message& result) const{
      QI_FUNCTOR_ASSUME_NBR_PARAMS(params, 0);

      (fObject->*fFunction)();
    };

  private:
    C            *fObject;
    FunctionType  fFunction;
  };


  template <typename P0,  typename C>
  class MemberFunctor_1<P0,  C, void> : public Functor
  {
  public:
    typedef void(C::*FunctionType) (const P0 &p0);

    MemberFunctor_1(C *pObject, FunctionType pFunction)
      : fObject(pObject),
        fFunction(pFunction) {
    }

    void operator()(const P0 &p0) {
      (fObject->*fFunction)(p0);
    }

    void call(qi::serialization::Message &params, qi::serialization::Message& result) const{
      QI_FUNCTOR_ASSUME_NBR_PARAMS(params, 1);
      P0 p0;

      qi::serialization::serialize<P0>::read(params, p0);
      (fObject->*fFunction)(p0);
    };

  private:
    C            *fObject;
    FunctionType  fFunction;
  };


  template <typename P0, typename P1,  typename C>
  class MemberFunctor_2<P0, P1,  C, void> : public Functor
  {
  public:
    typedef void(C::*FunctionType) (const P0 &p0, const P1 &p1);

    MemberFunctor_2(C *pObject, FunctionType pFunction)
      : fObject(pObject),
        fFunction(pFunction) {
    }

    void operator()(const P0 &p0, const P1 &p1) {
      (fObject->*fFunction)(p0, p1);
    }

    void call(qi::serialization::Message &params, qi::serialization::Message& result) const{
      QI_FUNCTOR_ASSUME_NBR_PARAMS(params, 2);
      P0 p0;
      P1 p1;

      qi::serialization::serialize<P0>::read(params, p0);
      qi::serialization::serialize<P1>::read(params, p1);
      (fObject->*fFunction)(p0, p1);
    };

  private:
    C            *fObject;
    FunctionType  fFunction;
  };


  template <typename P0, typename P1, typename P2,  typename C>
  class MemberFunctor_3<P0, P1, P2,  C, void> : public Functor
  {
  public:
    typedef void(C::*FunctionType) (const P0 &p0, const P1 &p1, const P2 &p2);

    MemberFunctor_3(C *pObject, FunctionType pFunction)
      : fObject(pObject),
        fFunction(pFunction) {
    }

    void operator()(const P0 &p0, const P1 &p1, const P2 &p2) {
      (fObject->*fFunction)(p0, p1, p2);
    }

    void call(qi::serialization::Message &params, qi::serialization::Message& result) const{
      QI_FUNCTOR_ASSUME_NBR_PARAMS(params, 3);
      P0 p0;
      P1 p1;
      P2 p2;

      qi::serialization::serialize<P0>::read(params, p0);
      qi::serialization::serialize<P1>::read(params, p1);
      qi::serialization::serialize<P2>::read(params, p2);
      (fObject->*fFunction)(p0, p1, p2);
    };

  private:
    C            *fObject;
    FunctionType  fFunction;
  };


  template <typename P0, typename P1, typename P2, typename P3,  typename C>
  class MemberFunctor_4<P0, P1, P2, P3,  C, void> : public Functor
  {
  public:
    typedef void(C::*FunctionType) (const P0 &p0, const P1 &p1, const P2 &p2, const P3 &p3);

    MemberFunctor_4(C *pObject, FunctionType pFunction)
      : fObject(pObject),
        fFunction(pFunction) {
    }

    void operator()(const P0 &p0, const P1 &p1, const P2 &p2, const P3 &p3) {
      (fObject->*fFunction)(p0, p1, p2, p3);
    }

    void call(qi::serialization::Message &params, qi::serialization::Message& result) const{
      QI_FUNCTOR_ASSUME_NBR_PARAMS(params, 4);
      P0 p0;
      P1 p1;
      P2 p2;
      P3 p3;

      qi::serialization::serialize<P0>::read(params, p0);
      qi::serialization::serialize<P1>::read(params, p1);
      qi::serialization::serialize<P2>::read(params, p2);
      qi::serialization::serialize<P3>::read(params, p3);
      (fObject->*fFunction)(p0, p1, p2, p3);
    };

  private:
    C            *fObject;
    FunctionType  fFunction;
  };


  template <typename P0, typename P1, typename P2, typename P3, typename P4,  typename C>
  class MemberFunctor_5<P0, P1, P2, P3, P4,  C, void> : public Functor
  {
  public:
    typedef void(C::*FunctionType) (const P0 &p0, const P1 &p1, const P2 &p2, const P3 &p3, const P4 &p4);

    MemberFunctor_5(C *pObject, FunctionType pFunction)
      : fObject(pObject),
        fFunction(pFunction) {
    }

    void operator()(const P0 &p0, const P1 &p1, const P2 &p2, const P3 &p3, const P4 &p4) {
      (fObject->*fFunction)(p0, p1, p2, p3, p4);
    }

    void call(qi::serialization::Message &params, qi::serialization::Message& result) const{
      QI_FUNCTOR_ASSUME_NBR_PARAMS(params, 5);
      P0 p0;
      P1 p1;
      P2 p2;
      P3 p3;
      P4 p4;

      qi::serialization::serialize<P0>::read(params, p0);
      qi::serialization::serialize<P1>::read(params, p1);
      qi::serialization::serialize<P2>::read(params, p2);
      qi::serialization::serialize<P3>::read(params, p3);
      qi::serialization::serialize<P4>::read(params, p4);
      (fObject->*fFunction)(p0, p1, p2, p3, p4);
    };

  private:
    C            *fObject;
    FunctionType  fFunction;
  };


  template <typename P0, typename P1, typename P2, typename P3, typename P4, typename P5,  typename C>
  class MemberFunctor_6<P0, P1, P2, P3, P4, P5,  C, void> : public Functor
  {
  public:
    typedef void(C::*FunctionType) (const P0 &p0, const P1 &p1, const P2 &p2, const P3 &p3, const P4 &p4, const P5 &p5);

    MemberFunctor_6(C *pObject, FunctionType pFunction)
      : fObject(pObject),
        fFunction(pFunction) {
    }

    void operator()(const P0 &p0, const P1 &p1, const P2 &p2, const P3 &p3, const P4 &p4, const P5 &p5) {
      (fObject->*fFunction)(p0, p1, p2, p3, p4, p5);
    }

    void call(qi::serialization::Message &params, qi::serialization::Message& result) const{
      QI_FUNCTOR_ASSUME_NBR_PARAMS(params, 6);
      P0 p0;
      P1 p1;
      P2 p2;
      P3 p3;
      P4 p4;
      P5 p5;

      qi::serialization::serialize<P0>::read(params, p0);
      qi::serialization::serialize<P1>::read(params, p1);
      qi::serialization::serialize<P2>::read(params, p2);
      qi::serialization::serialize<P3>::read(params, p3);
      qi::serialization::serialize<P4>::read(params, p4);
      qi::serialization::serialize<P5>::read(params, p5);
      (fObject->*fFunction)(p0, p1, p2, p3, p4, p5);
    };

  private:
    C            *fObject;
    FunctionType  fFunction;
  };


  template <typename P0, typename P1, typename P2, typename P3, typename P4, typename P5, typename P6,  typename C>
  class MemberFunctor_7<P0, P1, P2, P3, P4, P5, P6,  C, void> : public Functor
  {
  public:
    typedef void(C::*FunctionType) (const P0 &p0, const P1 &p1, const P2 &p2, const P3 &p3, const P4 &p4, const P5 &p5, const P6 &p6);

    MemberFunctor_7(C *pObject, FunctionType pFunction)
      : fObject(pObject),
        fFunction(pFunction) {
    }

    void operator()(const P0 &p0, const P1 &p1, const P2 &p2, const P3 &p3, const P4 &p4, const P5 &p5, const P6 &p6) {
      (fObject->*fFunction)(p0, p1, p2, p3, p4, p5, p6);
    }

    void call(qi::serialization::Message &params, qi::serialization::Message& result) const{
      QI_FUNCTOR_ASSUME_NBR_PARAMS(params, 7);
      P0 p0;
      P1 p1;
      P2 p2;
      P3 p3;
      P4 p4;
      P5 p5;
      P6 p6;

      qi::serialization::serialize<P0>::read(params, p0);
      qi::serialization::serialize<P1>::read(params, p1);
      qi::serialization::serialize<P2>::read(params, p2);
      qi::serialization::serialize<P3>::read(params, p3);
      qi::serialization::serialize<P4>::read(params, p4);
      qi::serialization::serialize<P5>::read(params, p5);
      qi::serialization::serialize<P6>::read(params, p6);
      (fObject->*fFunction)(p0, p1, p2, p3, p4, p5, p6);
    };

  private:
    C            *fObject;
    FunctionType  fFunction;
  };


  template <typename P0, typename P1, typename P2, typename P3, typename P4, typename P5, typename P6, typename P7,  typename C>
  class MemberFunctor_8<P0, P1, P2, P3, P4, P5, P6, P7,  C, void> : public Functor
  {
  public:
    typedef void(C::*FunctionType) (const P0 &p0, const P1 &p1, const P2 &p2, const P3 &p3, const P4 &p4, const P5 &p5, const P6 &p6, const P7 &p7);

    MemberFunctor_8(C *pObject, FunctionType pFunction)
      : fObject(pObject),
        fFunction(pFunction) {
    }

    void operator()(const P0 &p0, const P1 &p1, const P2 &p2, const P3 &p3, const P4 &p4, const P5 &p5, const P6 &p6, const P7 &p7) {
      (fObject->*fFunction)(p0, p1, p2, p3, p4, p5, p6, p7);
    }

    void call(qi::serialization::Message &params, qi::serialization::Message& result) const{
      QI_FUNCTOR_ASSUME_NBR_PARAMS(params, 8);
      P0 p0;
      P1 p1;
      P2 p2;
      P3 p3;
      P4 p4;
      P5 p5;
      P6 p6;
      P7 p7;

      qi::serialization::serialize<P0>::read(params, p0);
      qi::serialization::serialize<P1>::read(params, p1);
      qi::serialization::serialize<P2>::read(params, p2);
      qi::serialization::serialize<P3>::read(params, p3);
      qi::serialization::serialize<P4>::read(params, p4);
      qi::serialization::serialize<P5>::read(params, p5);
      qi::serialization::serialize<P6>::read(params, p6);
      qi::serialization::serialize<P7>::read(params, p7);
      (fObject->*fFunction)(p0, p1, p2, p3, p4, p5, p6, p7);
    };

  private:
    C            *fObject;
    FunctionType  fFunction;
  };


  template <typename P0, typename P1, typename P2, typename P3, typename P4, typename P5, typename P6, typename P7, typename P8,  typename C>
  class MemberFunctor_9<P0, P1, P2, P3, P4, P5, P6, P7, P8,  C, void> : public Functor
  {
  public:
    typedef void(C::*FunctionType) (const P0 &p0, const P1 &p1, const P2 &p2, const P3 &p3, const P4 &p4, const P5 &p5, const P6 &p6, const P7 &p7, const P8 &p8);

    MemberFunctor_9(C *pObject, FunctionType pFunction)
      : fObject(pObject),
        fFunction(pFunction) {
    }

    void operator()(const P0 &p0, const P1 &p1, const P2 &p2, const P3 &p3, const P4 &p4, const P5 &p5, const P6 &p6, const P7 &p7, const P8 &p8) {
      (fObject->*fFunction)(p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    void call(qi::serialization::Message &params, qi::serialization::Message& result) const{
      QI_FUNCTOR_ASSUME_NBR_PARAMS(params, 9);
      P0 p0;
      P1 p1;
      P2 p2;
      P3 p3;
      P4 p4;
      P5 p5;
      P6 p6;
      P7 p7;
      P8 p8;

      qi::serialization::serialize<P0>::read(params, p0);
      qi::serialization::serialize<P1>::read(params, p1);
      qi::serialization::serialize<P2>::read(params, p2);
      qi::serialization::serialize<P3>::read(params, p3);
      qi::serialization::serialize<P4>::read(params, p4);
      qi::serialization::serialize<P5>::read(params, p5);
      qi::serialization::serialize<P6>::read(params, p6);
      qi::serialization::serialize<P7>::read(params, p7);
      qi::serialization::serialize<P8>::read(params, p8);
      (fObject->*fFunction)(p0, p1, p2, p3, p4, p5, p6, p7, p8);
    };

  private:
    C            *fObject;
    FunctionType  fFunction;
  };

}
}
#endif  // _QI_FUNCTORS_DETAIL_VOIDMEMBERFUNCTOR_HXX_