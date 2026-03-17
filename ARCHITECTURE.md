# Recipe App Architecture Blueprint

## Core Features
- **Recipes**: Add, Edit, Delete (Soft delete to trash), Share, Notes, Private/Public/Shared, Copy recipe, Photos, Video links.
- **Users**: Create user, Update username.
- **Auth**: JWT Login.
- **Nutrition**: Calculate nutrition from ingredients, Recalculate based on portion size.
- **Search**: By category, ingredient, name, description.
- **Categories**: Add category, Add recipe to multiple categories, Edit category name, Nested subcategories.
- **Trash**: Removed recipes go to a trash bin first.

## Database Schema (Target)
- `users`: id (UUID), username, email, created_at, updated_at
- `recipes`: id (UUID), user_id (FK), name, description, is_public, shared_with_userlist, copied_from_id, is_trashed, created_at, updated_at
- `ingredients`: id (UUID), name, calories_per_100g, protein_per_100g, fat_per_100g, carbs_per_100g
- `recipe_ingredients`: id (UUID), recipe_id (FK), ingredient_id (FK), quantity (Float)
- `categories`: id (UUID), name, parent_category_id (FK)
- `recipe_categories`: recipe_id (FK), category_id (FK)
- `recipe_notes`: id (UUID), recipe_id (FK), user_id (FK), note, created_at
- `recipe_shared_users`: recipe_id (FK), user_id (FK)
- `recipe_trash`: recipe_id (UUID), deleted_at