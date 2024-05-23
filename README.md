# SPL-Task2
BGU Micro-Service Framework and Compute Resource Management System
This project implements a simple Micro-Service framework and utilizes it to develop a system for managing the University's compute resources. It consists of two main parts:

Micro-Service Framework: A framework for building Micro-Service applications with support for message exchange between services using a shared Message Bus.
Compute Resource Management System: A system built on top of the Micro-Service framework to manage the University's compute resources, including training deep learning models, processing data, and handling conferences.

Features

Micro-Service Framework:

Message Bus for communication between Micro-Services
Support for Events (sent to a single subscriber) and Broadcasts (sent to all subscribers)
Round-robin event assignment to Micro-Services
Efficient synchronization and concurrency handling


Compute Resource Management System:

Students can create events to train and test deep learning models
GPU and CPU Micro-Services handle model training and data processing
Conference Micro-Services aggregate and publish successful model results
Timing and synchronization between Micro-Services and resources



Implementation Details
The Micro-Service framework is implemented using Java 8 and follows an object-oriented design. It includes classes for the Message Bus, Micro-Services, Events, and Broadcasts. The framework supports efficient concurrency and synchronization techniques.
The Compute Resource Management System consists of various Micro-Services, such as Student, GPU, CPU, and Conference services, along with supporting classes like Model, Data, and Cluster. It simulates the process of training deep learning models, processing data, and publishing successful results at conferences.
Getting Started

Clone the repository
Import the project into your preferred Java IDE
Build the project using Maven
Run the application with the provided input file

Input and Output
The application expects an input file in JSON format, containing information about students, GPUs, CPUs, conferences, tick time, and duration. The output will be a text file with details about trained models, published results, GPU/CPU time usage, and processed data batches.
