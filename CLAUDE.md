# English Learning Web App

## Overview

The goal of this project is to build an English learning platform that helps users collect, practice, and master English vocabulary using spaced repetition and multiple exercise types.

Instead of simply memorizing word lists, users will continuously review words through different kinds of exercises, making long-term retention much more effective.

---

# Core Concept

When users learn a new English word, they can save it to the application.

A word consists of shared (public) information and user-specific (private) learning progress.

## Public Data

Every word is shared between all users.

Example:

- Word
- Pronunciation
- Meaning
- Part of speech
- Synonyms
- Antonyms
- Example sentences
- Images
- Audio pronunciation
- Topics (Vehicle, Food, Business, etc.)

If another user saves the same word, the application reuses the existing word instead of creating a duplicate.

---

## Private Data

Each user has their own learning progress.

Example:

- Learned or not
- Learning date
- Review history
- Correct / incorrect answers
- Mastery level
- Last reviewed
- Next review date

This information is private for each user.

---

# Practice System

The application should provide various exercise types so users don't simply memorize answers.

## Exercise Types

### Fill in the Blank

Example

> I _____ to school every morning.

Answer:

> go

---

### Multiple Choice

What does **vehicle** mean?

- A fruit
- A machine used for transportation ✅
- A building
- A country

---

### Choose the Synonym

Word:

> happy

Options:

- joyful ✅
- angry
- cold
- expensive

---

### Choose the Antonym

Word:

> ancient

Options:

- modern ✅
- old
- historic
- traditional

---

### Image Guessing

Show an image and ask the user to choose the correct word.

Example:

(Image of a bicycle)

Options:

- bicycle ✅
- airplane
- train
- helmet

---

### Listening

Play audio and ask the user to choose the correct word.

---

### Speaking

Show a word and ask the user to pronounce it.

Speech recognition can be used to evaluate pronunciation.

---

# Daily Practice

Users should receive different types of review sessions.

## Must Practice

Highest priority.

Contains words that the user answered incorrectly in previous sessions.

---

## Newly Learned

Words that were recently added.

These should appear frequently during the first few days.

---

## Old Review

Random review of older words.

For example:

- learned 1 month ago
- learned 3 months ago
- learned 6 months ago

This helps prevent forgetting.

---

# Dashboard

The dashboard should provide an overview of learning progress.

Metrics include:

- Total words learned
- Mastered words
- Words needing review
- Forgotten words
- Current learning streak
- Longest streak
- Accuracy rate
- Exercises completed
- Reviews completed today

Possible future additions:

- Weekly progress
- Monthly progress
- Learning heatmap
- Topic distribution
- Vocabulary growth chart

---

# Background Jobs

Several background jobs should automate content enrichment.

## Word Analysis

Automatically determine:

- Part of speech
    - Noun
    - Verb
    - Adjective
    - Adverb
    - Pronoun
    - Preposition
    - etc.

---

## Topic Classification

Automatically assign topics.

Examples:

- Food
- Vehicle
- Clothing
- Business
- Technology
- Sports
- Travel
- Health
- Nature

---

## Content Generation

Generate learning materials for each word.

Examples:

- Example sentences
- Fill-in-the-blank exercises
- Multiple-choice questions
- Synonym questions
- Antonym questions
- Image prompts
- Speaking prompts
- Listening exercises

---

## Difficulty Estimation

Estimate the difficulty level of each word.

Example:

- Beginner (A1)
- Elementary (A2)
- Intermediate (B1)
- Upper Intermediate (B2)
- Advanced (C1)
- Proficient (C2)

---

# Learning Algorithm

The application should use a spaced repetition algorithm.

Possible inputs:

- Number of correct answers
- Number of incorrect answers
- Time since last review
- Confidence score
- Exercise type
- User response time

These factors determine the next review date.

---

# Future Features

- AI explanations for words
- AI-generated mnemonics
- AI chat partner
- Sentence correction
- Grammar correction
- Personal vocabulary recommendations
- Import words from Kindle
- Import browser highlights
- Chrome extension
- Mobile application
- Offline mode
- Gamification (XP, levels, badges)
- Leaderboards (optional)
- Friends and study groups
- Daily challenges
- Word collections
- Flashcards
- Dark mode

---

# Tech Ideas

## Backend

- Spring Boot
- PostgreSQL
- Redis
- Quartz / Scheduled Jobs
- Elasticsearch (optional)

## Frontend

- React
- TypeScript
- React Query
- Tailwind CSS

## AI

- Generate exercises
- Generate example sentences
- Topic classification
- Difficulty estimation
- Speech evaluation
- Automatic tagging

---

# MVP

## User

- Register/Login
- Save words
- Search words
- View learned words

## Practice

- Fill in the blank
- Multiple choice
- Synonym
- Antonym

## Dashboard

- Learned words
- Need review
- Mastered words
- Streak

## Background Jobs

- Part-of-speech tagging
- Topic tagging
- Example sentence generation
- Exercise generation

---

# Long-Term Vision

Build an intelligent English learning platform where users continuously grow their vocabulary through personalized practice, AI-generated exercises, and spaced repetition. The system should minimize manual work by automatically enriching each word with useful learning content while adapting review schedules to each user's performance.