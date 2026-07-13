-- answered_option now stores a JSON array of selected options
ALTER TABLE user_practice_results
    ALTER COLUMN answered_option TYPE TEXT;
