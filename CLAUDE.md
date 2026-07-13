# English Learning Web App

---

# Code Structure & Technology Stack

## Monorepo Layout

```
english-master/
├── english-master-service/   # Spring Boot backend
└── english-master-ui/        # React frontend
```

## Authentication
To use the application, the user need to login first.
With local:
- Username: nntan041299@gmail.com
- Password: 123123123

---

## Backend — `english-master-service`

**Tech Stack**
- Java + Spring Boot 4.0.0
- Spring Security (JWT-based auth + token blacklist)
- Spring Data JPA + PostgreSQL
- Flyway (database migrations under `src/main/resources/db/migration/`)
- Spring Cloud OpenFeign (HTTP clients for Google OAuth2 and Gemini AI)
- MapStruct (entity ↔ DTO mapping)
- Lombok
- Docker / docker-compose

**Package Structure**

```
com.nntan041299.englishmasterservice
├── ai/                        # AI integration
│   ├── AIService.java         # Abstract AI service interface
│   ├── AiPromptManager.java   # Loads prompts from ai-promt.properties
│   └── gemini/                # Google Gemini client + DTOs
├── auth/                      # Authentication & users
│   ├── controller/            # AuthController, GoogleAuthenticationController, UserController
│   ├── dto/                   # Request/Response DTOs
│   ├── entity/                # User, Role
│   ├── mapper/                # UserMapper (MapStruct)
│   ├── repository/            # UserRepository
│   └── service/               # AuthService, GoogleOauth2Impl, OAuthStateStore
├── common/                    # Shared utilities
│   ├── advice/                # ApiResponseWrapperAdvice (wraps all responses)
│   ├── config/                # JacksonConfig, AuditingConfig
│   ├── dto/                   # ApiResponse, PageResponse, ErrorResponse
│   ├── entity/                # BaseEntity (auditing base class)
│   ├── exception/             # GlobalExceptionHandler
│   └── util/                  # StringUtils
├── meaning/                   # Word meaning domain
│   ├── dto/                   # MeaningAiResponse
│   ├── entity/                # Meaning, PartOfSpeech
│   └── repository/            # MeaningRepository
├── practice/                  # Practice domain
│   ├── controller/            # PracticeController
│   ├── dto/                   # PracticeResponse, AnswerPracticeRequest/Response, PracticeAiResponse
│   ├── entity/                # Practice, PracticeOption, UserPractice, UserPracticeResult,
│   │                          #   LearningTracking, converters
│   ├── job/                   # WordMeaningPracticeGenerationJob, PracticeAssignmentJob
│   ├── repository/            # PracticeRepository, UserPracticeRepository, UserPracticeResultRepository
│   └── service/               # PracticeService
├── security/                  # JWT filter, SecurityConfig, token blacklist
└── word/                      # Word domain
    ├── controller/            # WordController
    ├── dto/                   # SaveWordRequest, WordResponse, WordMeaningResponse, DashboardResponse
    ├── entity/                # Word, UserWord, LearningLevel
    ├── job/                   # WordEnrichmentJob
    ├── mapper/                # WordMapper
    ├── repository/            # WordRepository, UserWordRepository, WordAvgPoint
    └── service/               # WordService
```

**Database Migrations (Flyway)**

| Version | Table |
|---------|-------|
| V0.00.00.1 | users |
| V0.00.00.2 | revoked_tokens |
| V0.00.00.3 | words |
| V0.00.00.4 | user_words |
| V0.00.00.5 | meanings |
| V0.00.00.6 | practices |
| V0.00.00.7 | user_practices |
| V0.00.00.8 | user_practice_results |

---

## Frontend — `english-master-ui`

**Tech Stack**
- React 18 + TypeScript
- Vite (build tool)
- Tailwind CSS 4
- Redux Toolkit + React Redux (global state)
- TanStack React Query (server state / data fetching)
- Axios (HTTP client)
- PrimeIcons (icon library)
- React Router DOM v6
- ESLint + Prettier + Husky (code quality)

**Source Structure**

```
src/
├── components/        # Shared UI components (Header, SideBar, Loading, PageLoader, AuthHero, PasswordField, EmptyState, CircleProgress, LevelBadge)
├── config/            # API base URL config
├── context/           # AuthProvider (React context)
├── hook/              # Custom hooks (useLogin, useRegister, usePractice, useGoogleLoginCallBack, useClickOutside)
├── layouts/           # Layout wrapper
├── pages/             # Route-level pages
│   ├── Dashboard/
│   ├── Login/
│   ├── SignUp/
│   ├── Vocabulary/
│   ├── Practice/
│   ├── GoogleOauthCallBack/
│   └── NotFound/
├── redux/             # Redux store, root reducer, user slice (actions/reducer/selectors/types)
├── rest/              # Axios instance, endpoint constants, request helpers
├── router/            # AppRouter, AuthRouter, route definitions
├── service/           # API call functions grouped by domain (auth, user, word, practice)
└── styles/            # Global CSS (base, theme, component-level)
```
