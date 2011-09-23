#pragma once
/*
*  Author(s):
*  - Cedric Gestes <gestes@aldebaran-robotics.com>
*  - Chris  Kilner <ckilner@aldebaran-robotics.com>
*
*  Copyright (C) 2010 Aldebaran Robotics
*/


#ifndef _QIMESSAGING_MASTER_HPP_
#define _QIMESSAGING_MASTER_HPP_

#include <string>
#include <boost/scoped_ptr.hpp>
#include <qimessaging/context.hpp>
#include <qimessaging/api.hpp>

namespace qi {
  namespace detail {
    class MasterImpl;
  }

  /// <summary> A central directory that allows Services and Topics to
  /// be discovered.
  /// </summary>
  /// \ingroup Messaging
  class QIMESSAGING_API Master {
  public:

    /// <summary> Constructor. </summary>
    /// <param name="masterAddress">
    /// The master address.
    /// e.g. 127.0.0.1:5555
    /// </param>
    /// <seealso cref="qi::Server"/>
    /// <seealso cref="qi::Client"/>
    /// <seealso cref="qi::Publisher"/>
    /// <seealso cref="qi::Subscriber"/>
    Master(const std::string& masterAddress = "127.0.0.1:5555", qi::Context *ctx = NULL);

    /// <summary>Destructor. </summary>
    virtual ~Master();

    /// <summary>Runs the master. </summary>
    void run();

    bool isInitialized() const;

  private:
    /// <summary> The private implementation </summary>
    boost::scoped_ptr<detail::MasterImpl> _impl;
  };

}

#endif  // _QIMESSAGING_MASTER_HPP_
