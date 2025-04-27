# Документация на конфигурацию detekt для пректа Meera
# https://nomera.atlassian.net/wiki/spaces/NOMIT/pages/3328442381/Detekt

#### https://docs.github.com/ru/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax

#### https://github.com/detekt/detekt/blob/main/config/detekt/detekt.yml

#### https://github.com/detekt/detekt/issues/3212 - комент про таску чтобы сгенерировать конфиг - detektGenerateConfig

#### Для запуска запустить таску - detekt

#### Дока на гите - https://github.com/detekt/detekt

#### Дока на сайте - https://detekt.dev/docs/rules/comments

#### Состояние проекта на день подключения в эпике - https://nomera.atlassian.net/browse/PO-823

# Отключены следующие рулы:

- LongParameterList
- TooManyFunctions
- CyclomaticComplexMethod
  [Complex methods are hard to understand and read. It might not be obvious what side-effects a complex method has.
  Prefer splitting up complex methods into smaller methods that are in turn easier to understand.
  Smaller methods can also be named much clearer which leads to improved readability of the code.
  This rule uses McCabe's Cyclomatic Complexity (MCC) metric to measure the number of linearly
  independent paths through a function's source code (https://www.ndepend.com/docs/code-metrics#CC).
  The higher the number of independent paths, the more complex a method is.
  Complex methods use too many of the following statements. Each one of them adds one to the complexity count.]
- NewLineAtEndOfFile
- MagicNumber
- MaxLineLength  - [ВКЛЮЧЕН]
- MayBeConst - [ВКЛЮЧЕН]
- WildcardImport - [ВКЛЮЧЕН]
- UnusedPrivateMember - [ВКЛЮЧЕН]
- UseCheckOrError - [ВКЛЮЧЕН]
- UtilityClassWithPublicConstructor - [ВКЛЮЧЕН]
- ForbiddenComment
- FunctionOnlyReturningConstant - [ВКЛЮЧЕН]
- SerialVersionUIDInSerializableClass
- ExplicitItLambdaParameter - [ВКЛЮЧЕН]
- UnnecessaryAbstractClass - [ВКЛЮЧЕН]
- LoopWithTooManyJumpStatements - [ВКЛЮЧЕН]
- EnumNaming - [ВКЛЮЧЕН]
- VariableNaming - [К ОБСУЖДЕНИЮ]
- InvalidPackageDeclaration - [ВКЛЮЧЕН]
- MatchingDeclarationName
- ImplicitDefaultLocale
- UselessPostfixExpression
- SafeCast
- UnusedPrivateClass - [ВКЛЮЧЕН]
- ModifierOrder - [ВКЛЮЧЕН]
- ConstructorParameterNaming
- MemberNameEqualsClassName
- FunctionNaming
- SpreadOperator
- FunctionParameterNaming
- TopLevelPropertyNaming
- PackageNaming
- EmptyFunctionBlock - [ВКЛЮЧЕН]
- EmptyClassBlock - [ВКЛЮЧЕН]
- EmptyKtFile - [ВКЛЮЧЕН]
- UnusedImports - [ВКЛЮЧЕН]
- NestedBlockDepth
- ComplexCondition
- LargeClass

## Exception

- IteratorNotThrowingNoSuchElementException - [НЕ НУЖНО]
- SwallowedException - [ВКЛЮЧЕН]
- ThrowingExceptionsWithoutMessageOrCause - [ВКЛЮЧЕН]
- TooGenericExceptionThrown
  [RuntimeException is a too generic Exception. Prefer throwing specific exceptions that indicate a specific error case.
  Exception is a too generic Exception. Prefer throwing specific exceptions that indicate a specific error case.]
- EmptyElseBlock - [ВКЛЮЧЕН]
- InstanceOfCheckForException - [ВКЛЮЧЕН]
- EmptyIfBlock - [ВКЛЮЧЕН]
