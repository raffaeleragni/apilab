# API-LAB

This is a stack for building scalable APIs. It's low level, efficient, and takes advantage of what java is good on: post-JIT performance at runtime.

For that reason all the runtime 'magic' such as class path scanning and variable metaprogramming is reduced to the minimum or eliminated completely.

The stack is composed of three high level components:
  - endpoints: the actual api endpoints, this is the component usually exposed to use
  - consumers: queue consumers for async processing
  - scheduled: time based event triggers

The stack assumes these services to be always available:
 - postgres
 - rabbitmq
 - redis

## Templates

Templates available at https://github.com/raffaeleragni/apilab-template

## History of the library

This library is a continuation of my work from Tinder https://github.com/raffaeleragni/tinder
