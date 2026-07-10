import { combineReducers, UnknownAction } from "redux";

import { reducer as userReducer } from "./user";

export const RESET_STORE = "RESET_STORE";

const combinedReducer = combineReducers({
  user: userReducer,
});

type RootState = ReturnType<typeof combinedReducer>;

const rootReducer = (state: RootState | undefined, action: UnknownAction) => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  if (action.type === RESET_STORE)
    return combinedReducer(undefined, action as any);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return combinedReducer(state, action as any);
};

export default rootReducer;
