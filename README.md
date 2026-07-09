# English Master

An English learning platform that helps users collect, practice, and master English vocabulary using spaced repetition and multiple exercise types.

Instead of simply memorizing word lists, users continuously review words through different kinds of exercises, making long-term retention much more effective.

---

# Core Concept

When users learn a new English word, they can save it to the application.

A word consists of shared (public) information and user-specific (private) learning progress.

## Public Data

Every word is shared between all users.

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

- Learned or not
- Learning date
- Review history
- Correct / incorrect answers
- Mastery level
- Last reviewed
- Next review date

---

# Practice System

The application provides various exercise types so users don't simply memorize answers.

## Exercise Types

### Fill in the Blank

> I _____ to school every morning. → **go**

### Multiple Choice

What does **vehicle** mean?
- A fruit
- A machine used for transportation ✅
- A building
- A country

### Choose the Synonym / Antonym

Given a word, pick its synonym or antonym from multiple options.

### Image Guessing

Show an image and ask the user to choose the correct word.

### Listening

Play audio and ask the user to identify the correct word.

### Speaking

Show a word and ask the user to pronounce it. Speech recognition evaluates pronunciation.

---

# Daily Practice

## Must Practice

Highest priority. Contains words the user answered incorrectly in previous sessions.

## Newly Learned

Words recently added. Appear frequently during the first few days.

## Old Review

Random review of older words (e.g. learned 1, 3, or 6 months ago) to prevent forgetting.

---

# Dashboard

Overview of learning progress:

- Total words learned
- Mastered words
- Words needing review
- Forgotten words
- Current learning streak
- Longest streak
- Accuracy rate
- Exercises completed
- Reviews completed today

---

# Background Jobs

Automated jobs enrich word content:

- **Word Enrichment** — determines part of speech, difficulty, topics via AI
- **Practice Generation** — generates fill-in-the-blank, multiple-choice, synonym/antonym exercises via AI
- **Practice Assignment** — assigns generated practices to users based on their word list

---

# Learning Algorithm

Spaced repetition determines the next review date based on:

- Number of correct / incorrect answers
- Time since last review
- Confidence score
- Exercise type
- User response time

---

# Long-Term Vision

Build an intelligent English learning platform where users continuously grow their vocabulary through personalized practice, AI-generated exercises, and spaced repetition — with minimal manual work by automatically enriching each word with useful learning content.

## Future Features

- AI explanations and mnemonics
- AI chat partner
- Grammar and sentence correction
- Personal vocabulary recommendations
- Import from Kindle / browser highlights / Chrome extension
- Mobile app + offline mode
- Gamification (XP, levels, badges, leaderboards)
- Friends, study groups, daily challenges
- Flashcards, dark mode
