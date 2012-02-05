/*
**
** Author(s):
**  - Cedric GESTES <gestes@aldebaran-robotics.com>
**
** Copyright (C) 2010, 2011, 2012 Aldebaran Robotics
*/

#ifndef _QIMESSAGING_SERVER_H_
#define _QIMESSAGING_SERVER_H_

#include <qimessaging/message.h>
#include <qimessaging/object.h>

#ifdef __cplusplus
extern "C"
{
#endif

  typedef struct {} qi_server_t;

  qi_server_t *qi_server_create(const char *name);
  void         qi_server_destroy(qi_server_t *server);
  void         qi_server_connect(qi_server_t *server, const char *address);

  int          qi_server_register_object(qi_server_t *server, const char *address, qi_object_t *object);
  int          qi_server_unregister_object(qi_server_t *server, const char *address);

#ifdef __cplusplus
}
#endif

#endif  // _QIMESSAGING_SERVER_H_